package com.github.javachaos.javaneuralnetwork.jbrain.vision;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.github.javachaos.javaneuralnetwork.jbrain.sensors.camera.RGBData;

/**
 * Maintains a bounded temporal history of visual frames and computes simple
 * low-level visual features from them.
 *
 * @author fred
 *
 */
public class VisualCortex {

	private static final int DEFAULT_HISTORY_LIMIT = 16;
	private static final double DEFAULT_CHANGE_THRESHOLD = 24.0;
	private static final double RGB_MAX_VALUE = 255.0;

	private final int width;
	private final int height;
	private final int historyLimit;
	private final Deque<VisualFrame> frames;

	/**
	 * Construct a cortex for one-dimensional visual samples.
	 */
	public VisualCortex() {
		this(1, 1, DEFAULT_HISTORY_LIMIT);
	}

	/**
	 * Construct a cortex for the given frame shape.
	 *
	 * @param width
	 * 		the frame width in pixels.
	 * @param height
	 * 		the frame height in pixels.
	 */
	public VisualCortex(final int width, final int height) {
		this(width, height, DEFAULT_HISTORY_LIMIT);
	}

	/**
	 * Construct a cortex for the given frame shape and history capacity.
	 *
	 * @param width
	 * 		the frame width in pixels.
	 * @param height
	 * 		the frame height in pixels.
	 * @param historyLimit
	 * 		the maximum number of recent frames to keep.
	 */
	public VisualCortex(final int width, final int height, final int historyLimit) {
		if (width <= 0) {
			throw new IllegalArgumentException("Width must be positive.");
		}
		if (height <= 0) {
			throw new IllegalArgumentException("Height must be positive.");
		}
		if (historyLimit <= 0) {
			throw new IllegalArgumentException("History limit must be positive.");
		}
		this.width = width;
		this.height = height;
		this.historyLimit = historyLimit;
		this.frames = new ArrayDeque<>();
	}

	/**
	 * Observe a new visual frame and add it to the front of the temporal history.
	 *
	 * @param pixels
	 * 		the frame pixels in row-major order.
	 * @return
	 * 		the immutable frame recorded by this cortex.
	 */
	public VisualFrame observe(final List<RGBData> pixels) {
		Objects.requireNonNull(pixels, "pixels");
		int expectedPixelCount = width * height;
		if (pixels.size() != expectedPixelCount) {
			throw new IllegalArgumentException("Expected " + expectedPixelCount
					+ " pixels but received " + pixels.size() + ".");
		}

		VisualFrame frame = new VisualFrame(width, height, copyPixels(pixels));
		frames.addFirst(frame);
		while (frames.size() > historyLimit) {
			frames.removeLast();
		}
		return frame;
	}

	/**
	 * Get the most recent visual frame.
	 *
	 * @return
	 * 		the latest frame, or an empty optional when no frames have been observed.
	 */
	public Optional<VisualFrame> latestFrame() {
		return Optional.ofNullable(frames.peekFirst());
	}

	/**
	 * Get the frame observed immediately before the latest frame.
	 *
	 * @return
	 * 		the previous frame, when at least two frames have been observed.
	 */
	public Optional<VisualFrame> previousFrame() {
		if (frames.size() < 2) {
			return Optional.empty();
		}
		return Optional.of(frames.stream().skip(1).findFirst().orElseThrow());
	}

	/**
	 * Analyze motion between the latest two frames using the default threshold.
	 *
	 * @return
	 * 		a motion summary, or an empty optional when fewer than two frames exist.
	 */
	public Optional<MotionSummary> latestMotion() {
		return latestMotion(DEFAULT_CHANGE_THRESHOLD);
	}

	/**
	 * Analyze motion between the latest two frames.
	 *
	 * @param changeThreshold
	 * 		the per-pixel RGB distance required to count as changed.
	 * @return
	 * 		a motion summary, or an empty optional when fewer than two frames exist.
	 */
	public Optional<MotionSummary> latestMotion(final double changeThreshold) {
		Optional<VisualFrame> latest = latestFrame();
		Optional<VisualFrame> previous = previousFrame();
		if (latest.isEmpty() || previous.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(latest.orElseThrow().compareTo(previous.orElseThrow(), changeThreshold));
	}

	/**
	 * Get the current frame count.
	 *
	 * @return
	 * 		the number of frames in the visual history.
	 */
	public int frameCount() {
		return frames.size();
	}

	/**
	 * Get a snapshot of the current frame history, newest frame first.
	 *
	 * @return
	 * 		an immutable list of recent frames.
	 */
	public List<VisualFrame> recentFrames() {
		return List.copyOf(frames);
	}

	/**
	 * Clear all remembered frames.
	 */
	public void clear() {
		frames.clear();
	}

	private static List<RGBData> copyPixels(final List<RGBData> pixels) {
		List<RGBData> copy = new ArrayList<>(pixels.size());
		for (RGBData pixel : pixels) {
			Objects.requireNonNull(pixel, "pixel");
			copy.add(new RGBData(pixel.getRed(), pixel.getGreen(), pixel.getBlue()));
		}
		return List.copyOf(copy);
	}

	/**
	 * A single visual frame and its low-level feature summary.
	 *
	 * @param width
	 * 		the frame width.
	 * @param height
	 * 		the frame height.
	 * @param pixels
	 * 		the immutable pixels in row-major order.
	 */
	public record VisualFrame(int width, int height, List<RGBData> pixels) {

		/**
		 * Construct a visual frame.
		 *
		 * @param width
		 * 		the frame width.
		 * @param height
		 * 		the frame height.
		 * @param pixels
		 * 		the pixels in row-major order.
		 */
		public VisualFrame {
			if (width <= 0) {
				throw new IllegalArgumentException("Width must be positive.");
			}
			if (height <= 0) {
				throw new IllegalArgumentException("Height must be positive.");
			}
			Objects.requireNonNull(pixels, "pixels");
			if (pixels.size() != width * height) {
				throw new IllegalArgumentException("Pixel count does not match frame dimensions.");
			}
			pixels = copyPixels(pixels);
		}

		/**
		 * Get the pixel at the given coordinate.
		 *
		 * @param x
		 * 		the x coordinate.
		 * @param y
		 * 		the y coordinate.
		 * @return
		 * 		the pixel at {@code x, y}.
		 */
		public RGBData pixelAt(final int x, final int y) {
			if (x < 0 || x >= width || y < 0 || y >= height) {
				throw new IllegalArgumentException("Pixel coordinate is out of bounds.");
			}
			RGBData pixel = pixels.get(y * width + x);
			return new RGBData(pixel.getRed(), pixel.getGreen(), pixel.getBlue());
		}

		@Override
		public List<RGBData> pixels() {
			return copyPixels(pixels);
		}

		/**
		 * Compute an average color and brightness summary for this frame.
		 *
		 * @return
		 * 		the frame summary.
		 */
		public VisualSummary summarize() {
			double red = 0.0;
			double green = 0.0;
			double blue = 0.0;
			double minBrightness = Double.POSITIVE_INFINITY;
			double maxBrightness = Double.NEGATIVE_INFINITY;
			for (RGBData pixel : pixels) {
				red += pixel.getRed();
				green += pixel.getGreen();
				blue += pixel.getBlue();
				double brightness = brightness(pixel);
				minBrightness = Math.min(minBrightness, brightness);
				maxBrightness = Math.max(maxBrightness, brightness);
			}
			int count = pixels.size();
			double averageRed = red / count;
			double averageGreen = green / count;
			double averageBlue = blue / count;
			double averageBrightness = (averageRed + averageGreen + averageBlue) / (3.0 * RGB_MAX_VALUE);
			return new VisualSummary(averageRed, averageGreen, averageBlue,
					averageBrightness, minBrightness, maxBrightness);
		}

		/**
		 * Convert this frame into normalized grayscale brightness values.
		 *
		 * @return
		 * 		a normalized brightness vector with one value per pixel.
		 */
		public double[] toBrightnessVector() {
			double[] values = new double[pixels.size()];
			for (int i = 0; i < pixels.size(); i++) {
				values[i] = brightness(pixels.get(i));
			}
			return values;
		}

		/**
		 * Compare this frame to a previous frame.
		 *
		 * @param previous
		 * 		the previous frame.
		 * @param changeThreshold
		 * 		the per-pixel RGB distance required to count as changed.
		 * @return
		 * 		a motion summary between the two frames.
		 */
		public MotionSummary compareTo(final VisualFrame previous, final double changeThreshold) {
			Objects.requireNonNull(previous, "previous");
			if (changeThreshold < 0.0) {
				throw new IllegalArgumentException("Change threshold must be non-negative.");
			}
			if (width != previous.width || height != previous.height) {
				throw new IllegalArgumentException("Frames must have the same dimensions.");
			}

			int changedPixels = 0;
			double totalDelta = 0.0;
			double maxDelta = 0.0;
			double weightedX = 0.0;
			double weightedY = 0.0;
			double motionWeight = 0.0;

			for (int i = 0; i < pixels.size(); i++) {
				RGBData current = pixels.get(i);
				RGBData past = previous.pixels.get(i);
				double delta = colorDistance(current, past);
				totalDelta += delta;
				maxDelta = Math.max(maxDelta, delta);
				if (delta >= changeThreshold) {
					changedPixels++;
					int x = i % width;
					int y = i / width;
					weightedX += x * delta;
					weightedY += y * delta;
					motionWeight += delta;
				}
			}

			double meanDelta = totalDelta / pixels.size();
			double changedRatio = changedPixels / (double) pixels.size();
			double centerX = motionWeight == 0.0 ? -1.0 : weightedX / motionWeight;
			double centerY = motionWeight == 0.0 ? -1.0 : weightedY / motionWeight;
			return new MotionSummary(changedPixels, changedRatio, meanDelta, maxDelta, centerX, centerY);
		}

		private static double brightness(final RGBData pixel) {
			return (pixel.getRed() + pixel.getGreen() + pixel.getBlue()) / (3.0 * RGB_MAX_VALUE);
		}

		private static double colorDistance(final RGBData left, final RGBData right) {
			double red = left.getRed() - right.getRed();
			double green = left.getGreen() - right.getGreen();
			double blue = left.getBlue() - right.getBlue();
			return Math.sqrt(red * red + green * green + blue * blue);
		}
	}

	/**
	 * Low-level color and brightness statistics for a visual frame.
	 *
	 * @param averageRed
	 * 		the average red channel value.
	 * @param averageGreen
	 * 		the average green channel value.
	 * @param averageBlue
	 * 		the average blue channel value.
	 * @param averageBrightness
	 * 		the normalized average brightness.
	 * @param minBrightness
	 * 		the normalized darkest pixel brightness.
	 * @param maxBrightness
	 * 		the normalized brightest pixel brightness.
	 */
	public record VisualSummary(
			double averageRed,
			double averageGreen,
			double averageBlue,
			double averageBrightness,
			double minBrightness,
			double maxBrightness) {
	}

	/**
	 * Pixel-change summary between two consecutive visual frames.
	 *
	 * @param changedPixels
	 * 		the number of pixels whose color-distance crossed the threshold.
	 * @param changedRatio
	 * 		the changed pixel count divided by the total pixel count.
	 * @param meanDelta
	 * 		the mean RGB distance between frames.
	 * @param maxDelta
	 * 		the maximum RGB distance between frames.
	 * @param centerX
	 * 		the weighted x-coordinate of motion, or {@code -1} when no motion exists.
	 * @param centerY
	 * 		the weighted y-coordinate of motion, or {@code -1} when no motion exists.
	 */
	public record MotionSummary(
			int changedPixels,
			double changedRatio,
			double meanDelta,
			double maxDelta,
			double centerX,
			double centerY) {
	}
}
