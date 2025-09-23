package cs3540;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;
import processing.event.MouseEvent;

public class IdearTwo extends PApplet {
	// when in doubt, consult the Processsing reference:
	// https://processing.org/reference/
	// The argument passed to main must match the class name
	public static void main(String[] args) {
		// Tell processing what class we want to run.
		PApplet.main("cs3540.IdearTwo");
	}

	int margin = 200; // set the margin around the squares
	final int padding = 50; // padding between buttons and also their width/height
	final int buttonSize = 40; // padding between buttons and also their width/height
	ArrayList<Integer> trials = new ArrayList<Integer>(); // contains the order of buttons that activate in the test
	int trialNum = 0; // the current trial number (indexes into trials array above)
	int startTime = 0; // time starts when the first click is captured
	int finishTime = 0; // records the time of the final click
	int hits = 0; // number of successful clicks
	int misses = 0; // number of missed clicks
	Robot robot; // initialized in setup
	
	// Flash settings 
	float flashHz = 7.0f;    // pulses per second
	int flashMin = 80;       // minimum brightness
	int flashMax = 255;      // maximum brightness

	int numRepeats = 1; // sets the number of times each button repeats in the test
	
	// === Selector state ===
	int selRow = 0, selCol = 0;      // default selector location (top-left)
	int gridCols = 4, gridRows = 4;
	
	// Movement sensitivity / debounce for 1-finger
	float accumDX = 0, accumDY = 0;
	int moveThreshold = 25;          // pixels of cursor motion to count as one "step"
	int moveCooldownMs = 90;         // min ms between steps to avoid jitter
	int lastMoveMs = 0;

	/**
	 * https://processing.org/reference/settings_.html#:~:text=The%20settings()%20method%20runs,commands%20in%20the%20Processing%20API.
	 */
	public void settings() {
		size(700, 700);
	}

	/**
	 * // https://processing.org/reference/setup_.html
	 */
	public void setup() {
		noCursor(); // hides the system cursor if you want
		noStroke(); // turn off all strokes, we're just using fills here (can change this if you
					// want)
		textFont(createFont("Arial", 16)); // sets the font to Arial size 16
		textAlign(CENTER);
		frameRate(60); // normally you can't go much higher than 60 FPS.
		ellipseMode(CENTER); // ellipses are drawn from the center (BUT RECTANGLES ARE NOT!)
		// rectMode(CENTER); //enabling will break the scaffold code, but you might find
		// it easier to work with centered rects

		try {
			robot = new Robot(); // create a "Java Robot" class that can move the system cursor
		} catch (AWTException e) {
			e.printStackTrace();
		}

		// ===DON'T MODIFY MY RANDOM ORDERING CODE==
		for (int i = 0; i < 16; i++) // generate list of targets and randomize the order
			// number of buttons in 4x4 grid
			for (int k = 0; k < numRepeats; k++)
				// number of times each button repeats
				trials.add(i);

		Collections.shuffle(trials); // randomize the order of the buttons
		System.out.println("trial order: " + trials); // print out order for reference

		surface.setLocation(0, 0);// put window in top left corner of screen (doesn't always work)
	}

	public void draw() {
		noCursor();
		background(0); // set background to black

		if (trialNum >= trials.size()) // check to see if test is over
		{
			float timeTaken = (finishTime - startTime) / 1000f;
			float penalty = constrain(((95f - ((float) hits * 100f / (float) (hits + misses))) * .2f), 0, 100);
			fill(255); // set fill color to white
			// write to screen (not console)
			text("Finished!", width / 2, height / 2);
			text("Hits: " + hits, width / 2, height / 2 + 20);
			text("Misses: " + misses, width / 2, height / 2 + 40);
			text("Accuracy: " + (float) hits * 100f / (float) (hits + misses) + "%", width / 2, height / 2 + 60);
			text("Total time taken: " + timeTaken + " sec", width / 2, height / 2 + 80);
			text("Average time for each button: " + nf((timeTaken) / (float) (hits + misses), 0, 3) + " sec", width / 2,
					height / 2 + 100);
			text("Average time for each button + penalty: "
					+ nf(((timeTaken) / (float) (hits + misses) + penalty), 0, 3) + " sec", width / 2,
					height / 2 + 140);
			return; // return, nothing else to do now test is over
		}

		fill(255); // set fill color to white
		text((trialNum + 1) + " of " + trials.size(), 40, 20); // display what trial the user is on

		for (int i = 0; i < 16; i++)// for all button
			drawButton(i); // draw button
		
		Rectangle selBounds = getButtonLocation(toIndex(selRow, selCol));
		// red with some transparency
		drawSelectorRing(selBounds);
		
		//removed cursor red
		//fill(255, 0, 0, 200); // set fill color to translucent red
		//ellipse(mouseX, mouseY, 20, 20); // draw user cursor as a circle with a diameter of 20

	}

	public void mousePressed() // test to see if hit was in target!
	{
		if (trialNum >= trials.size()) // check if task is done
			return;

		if (trialNum == 0) // check if first click, if so, record start time
			startTime = millis();

		if (trialNum == trials.size() - 1) // check if final click
		{
			finishTime = millis();
			// write to terminal some output:
			System.out.println("we're all done!");
		}

		Rectangle bounds = getButtonLocation(trials.get(trialNum));

		// check to see if cursor was inside button
		if ((mouseX > bounds.x && mouseX < bounds.x + bounds.width)
				&& (mouseY > bounds.y && mouseY < bounds.y + bounds.height)) // test to see if hit was within bounds
		{
			System.out.println("HIT! " + trialNum + " " + (millis() - startTime)); // success
			hits++;
		} else {
			System.out.println("MISSED! " + trialNum + " " + (millis() - startTime)); // fail
			misses++;
		}

		trialNum++; // Increment trial number

		// in this example design, I move the cursor back to the middle after each click
		// Note. When running from eclipse the robot class affects the whole screen not
		// just the GUI, so the mouse may move outside of the GUI.
		// robot.mouseMove(width/2, (height)/2); //on click, move cursor to roughly
		// center of window!
	}

	// probably shouldn't have to edit this method
	public Rectangle getButtonLocation(int i) // for a given button ID, what is its location and size
	{
		int x = (i % 4) * (padding + buttonSize) + margin;
		int y = (i / 4) * (padding + buttonSize) + margin;

		return new Rectangle(x, y, buttonSize, buttonSize);
	}

	// you can edit this method to change how buttons appear
	public void drawButton(int i) {
		  Rectangle bounds = getButtonLocation(i);

		  int targetId = trials.get(trialNum); // current lit button id
		  
		// Compute next id if it exists; otherwise -1
		  int nextId = (trialNum + 1 < trials.size()) ? trials.get(trialNum + 1) : -1;
		  
		  Rectangle targetBounds = getButtonLocation(targetId);

		  if (i == targetId) {
			    // Flashing
			    int a = flashLevel();
			    fill(0, 255, 255, a);
			    rect(bounds.x, bounds.y, bounds.width, bounds.height);
			  } else {
			    fill(200);
			    rect(bounds.x, bounds.y, bounds.width, bounds.height);
			  }

		  // If this is the NEXT target, overlay a purple bar
		  if (i == nextId) {
		    // draw three bar to indicate "next"
			  drawTripleBarInCell(bounds, 180, 0, 255, 230); // purple;
		  }

		  // Draw arrows on non-current cells pointing toward the current target
		  if (i != targetId) {
		    float dx = centerX(targetBounds) - centerX(bounds);
		    float dy = centerY(targetBounds) - centerY(bounds);
		    float angle = atan2(dy, dx);
		    drawArrowInCell(bounds, angle);
		  }
	}

	public void mouseMoved() {
		  // Accumulate how far the mouse has drifted since last move
		  accumDX += (mouseX - pmouseX);
		  accumDY += (mouseY - pmouseY);

		  int now = millis();
		  if (now - lastMoveMs < moveCooldownMs) return;

		  float ax = abs(accumDX), ay = abs(accumDY);
		  if (ax < moveThreshold && ay < moveThreshold) return;

		  // Decide direction by dominant axis
		  if (ax >= ay) {
		    moveSelector(accumDX > 0 ? +1 : -1, 0);
		  } else {
		    moveSelector(0, accumDY > 0 ? +1 : -1);
		  }

		  accumDX = accumDY = 0;
		  lastMoveMs = now;
		// https://processing.org/reference/mouseMoved_.html
	}
	
	// !!! Possible remove this because Processing does not support left and right
	public void mouseWheel(MouseEvent event) {
		  float delta = event.getCount(); // >0 scroll down, <0 scroll up
		  int dir = (delta > 0) ? +1 : -1;
		  // Move two cells in the scroll direction
		  moveSelector(0, 2 * dir);
		  // optional: small cooldown to avoid bursts
		  lastMoveMs = millis();
		}

	public void mouseDragged() {
		// can do stuff everytime the mouse is dragged
		// https://processing.org/reference/mouseDragged_.html
	}

	public void keyPressed() {
		// can use the keyboard if you wish
		// https://processing.org/reference/keyTyped_.html
		// https://processing.org/reference/keyCode.html
	}
	
	// --- helpers for centers and arrow drawing ---
	private float centerX(Rectangle r) { return r.x + r.width  * 0.5f; }
	private float centerY(Rectangle r) { return r.y + r.height * 0.5f; }

	/** Draws a small arrow inside the cell, pointing along 'angle' (radians). */
	private void drawArrowInCell(Rectangle cell, float angle) {
	  float cx = centerX(cell);
	  float cy = centerY(cell);
	
	  // sizes relative to the cell so it scales with your buttonSize
	  float halfShaft = cell.width * 0.18f;   // half length of the shaft line
	  float headLen   = cell.width * 0.12f;   // length of the arrow head
	  float headHalfH = cell.height * 0.08f;  // half height of arrow head
	  
	  pushStyle();
	  pushMatrix();
	  translate(cx, cy);
	  rotate(angle);

	  // shaft
	  stroke(60);
	  strokeWeight(3);
	  line(-halfShaft, 0, halfShaft, 0);

	  // head (filled triangle)
	  noStroke();
	  fill(60);
	  // triangle tip at +halfShaft, base slightly back
	  triangle(
	      halfShaft, 0,
	      halfShaft - headLen, -headHalfH,
	      halfShaft - headLen,  headHalfH
	  );

	  popMatrix();
	  popStyle();
	}  
	
	/** Draw three horizontal bars centered inside the cell. */
	private void drawTripleBarInCell(Rectangle cell, int r, int g, int b, int a) {
	  float cx = centerX(cell);
	  float cy = centerY(cell);

	  // Size/spacing as a fraction of the cell; tweak to taste
	  float totalW   = cell.width * 0.78f;   // width of each bar
	  float barH     = cell.height * 0.14f;  // height of each bar
	  float gap      = cell.height * 0.10f;  // vertical gap between bars
	  float cornerR  = barH * 0.45f;         // rounded corners

	  // Y positions for top/mid/bot, centered around cy
	  float midY = cy - barH * 0.5f;
	  float topY = midY - (barH + gap);
	  float botY = midY + (barH + gap);

	  float leftX = cx - totalW * 0.5f;

	  pushStyle();
	  noStroke();
	  fill(r, g, b, a);
	  rect(leftX, topY, totalW, barH, cornerR);
	  rect(leftX, midY, totalW, barH, cornerR);
	  rect(leftX, botY, totalW, barH, cornerR);
	  popStyle();
	}

	
	/** Returns a pulsing value between flashMin and flashMax based on time. */
	private int flashLevel() {
	  float t = millis() / 1000.0f;                // seconds
	  float phase = (sin(TWO_PI * flashHz * t) + 1f) * 0.5f;  // 0..1
	  return (int) lerp(flashMin, flashMax, phase);           // flashMin..flashMax
	}
	
	private void moveSelector(int dCol, int dRow) {
		  selCol = constrain(selCol + dCol, 0, gridCols - 1);
		  selRow = constrain(selRow + dRow, 0, gridRows - 1);
		}

	
	// Draw a colored "padding ring" inside the selected cell	
	private void drawSelectorRing(Rectangle cell) {
	  float pad = cell.width * 0.15f;      // how far the ring is inset
	  float ring = cell.width * 0.10f;     // ring thickness

	  pushStyle();
	  noFill();
	  stroke(255, 0, 0, 220);              // red ring, slightly transparent
	  strokeWeight(ring);

	  // draw an inset rectangle so the ring sits inside the cell
	  float x = cell.x + pad;
	  float y = cell.y + pad;
	  float w = cell.width  - 2 * pad;
	  float h = cell.height - 2 * pad;
	  rect(x, y, w, h, pad * 0.4f);        // rounded corners optional
	  popStyle();
	}


		// Index helpers
		private int toIndex(int row, int col) { return row * 4 + col; }

}
