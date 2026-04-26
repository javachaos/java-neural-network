package com.github.javachaos.javaneuralnetwork.jbrain.tests.vision;

import com.github.javachaos.javaneuralnetwork.jbrain.sensors.camera.RGBData;
import com.github.javachaos.javaneuralnetwork.jbrain.vision.VisualCortex;
import com.github.javachaos.javaneuralnetwork.jbrain.vision.VisualCortex.MotionSummary;
import com.github.javachaos.javaneuralnetwork.jbrain.vision.VisualCortex.VisualFrame;
import com.github.javachaos.javaneuralnetwork.jbrain.vision.VisualCortex.VisualSummary;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisualCortexTest {

	private static final double EPSILON = 1.0e-10;

	@Test
	final void testObservesFrameAndComputesSummary() {
		VisualCortex cortex = new VisualCortex(2, 2);

		VisualFrame frame = cortex.observe(List.of(
				new RGBData(255.0, 0.0, 0.0),
				new RGBData(0.0, 255.0, 0.0),
				new RGBData(0.0, 0.0, 255.0),
				new RGBData(255.0, 255.0, 255.0)));
		VisualSummary summary = frame.summarize();

		assertEquals(1, cortex.frameCount());
		assertEquals(frame, cortex.latestFrame().orElseThrow());
		assertEquals(127.5, summary.averageRed(), EPSILON);
		assertEquals(127.5, summary.averageGreen(), EPSILON);
		assertEquals(127.5, summary.averageBlue(), EPSILON);
		assertEquals(0.5, summary.averageBrightness(), EPSILON);
		assertEquals(1.0 / 3.0, summary.minBrightness(), EPSILON);
		assertEquals(1.0, summary.maxBrightness(), EPSILON);
		assertArrayEquals(new double[]{1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0, 1.0},
				frame.toBrightnessVector(), EPSILON);
	}

	@Test
	final void testHistoryIsBoundedAndNewestFirst() {
		VisualCortex cortex = new VisualCortex(1, 1, 2);
		VisualFrame first = cortex.observe(List.of(new RGBData(1.0, 1.0, 1.0)));
		VisualFrame second = cortex.observe(List.of(new RGBData(2.0, 2.0, 2.0)));
		VisualFrame third = cortex.observe(List.of(new RGBData(3.0, 3.0, 3.0)));

		assertEquals(2, cortex.frameCount());
		assertEquals(List.of(third, second), cortex.recentFrames());
		assertEquals(third, cortex.latestFrame().orElseThrow());
		assertEquals(second, cortex.previousFrame().orElseThrow());
		assertTrue(cortex.recentFrames().stream().noneMatch(first::equals));
	}

	@Test
	final void testFramePixelsAreDefensivelyCopied() {
		VisualCortex cortex = new VisualCortex(1, 1);
		ArrayList<RGBData> pixels = new ArrayList<>();
		RGBData original = new RGBData(10.0, 20.0, 30.0);
		pixels.add(original);

		VisualFrame frame = cortex.observe(pixels);
		original.setRed(250.0);
		pixels.clear();
		RGBData readPixel = frame.pixelAt(0, 0);
		readPixel.setGreen(250.0);
		frame.pixels().get(0).setBlue(250.0);

		assertEquals(10.0, frame.pixelAt(0, 0).getRed(), EPSILON);
		assertEquals(20.0, frame.pixelAt(0, 0).getGreen(), EPSILON);
		assertEquals(30.0, frame.pixelAt(0, 0).getBlue(), EPSILON);
		assertThrows(UnsupportedOperationException.class,
				() -> frame.pixels().add(new RGBData(1.0, 1.0, 1.0)));
	}

	@Test
	final void testLatestMotionComparesNewestTwoFrames() {
		VisualCortex cortex = new VisualCortex(2, 2);
		cortex.observe(List.of(
				new RGBData(0.0, 0.0, 0.0),
				new RGBData(0.0, 0.0, 0.0),
				new RGBData(0.0, 0.0, 0.0),
				new RGBData(0.0, 0.0, 0.0)));
		cortex.observe(List.of(
				new RGBData(0.0, 0.0, 0.0),
				new RGBData(255.0, 0.0, 0.0),
				new RGBData(0.0, 0.0, 0.0),
				new RGBData(0.0, 255.0, 0.0)));

		MotionSummary motion = cortex.latestMotion(100.0).orElseThrow();

		assertEquals(2, motion.changedPixels());
		assertEquals(0.5, motion.changedRatio(), EPSILON);
		assertEquals(127.5, motion.meanDelta(), EPSILON);
		assertEquals(255.0, motion.maxDelta(), EPSILON);
		assertEquals(1.0, motion.centerX(), EPSILON);
		assertEquals(0.5, motion.centerY(), EPSILON);
	}

	@Test
	final void testRejectsInvalidInputs() {
		assertThrows(IllegalArgumentException.class, () -> new VisualCortex(0, 1));
		assertThrows(IllegalArgumentException.class, () -> new VisualCortex(1, 0));
		assertThrows(IllegalArgumentException.class, () -> new VisualCortex(1, 1, 0));

		VisualCortex cortex = new VisualCortex(2, 2);
		assertThrows(IllegalArgumentException.class,
				() -> cortex.observe(List.of(new RGBData(1.0, 1.0, 1.0))));
		assertThrows(IllegalArgumentException.class,
				() -> cortex.observe(List.of(
						new RGBData(1.0, 1.0, 1.0),
						new RGBData(1.0, 1.0, 1.0),
						new RGBData(1.0, 1.0, 1.0),
						new RGBData(1.0, 1.0, 1.0))).compareTo(
								new VisualFrame(1, 1, List.of(new RGBData(1.0, 1.0, 1.0))), 0.0));
		assertThrows(IllegalArgumentException.class,
				() -> cortex.latestFrame().orElseThrow().compareTo(cortex.latestFrame().orElseThrow(), -1.0));
		assertThrows(IllegalArgumentException.class, () -> cortex.latestFrame().orElseThrow().pixelAt(2, 0));
	}
}
