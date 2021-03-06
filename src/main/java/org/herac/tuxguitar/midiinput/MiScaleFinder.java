package org.herac.tuxguitar.midiinput;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.herac.tuxguitar.gui.TuxGuitar;
import org.herac.tuxguitar.gui.tools.scale.ScaleManager;
import org.herac.tuxguitar.util.TGSynchronizer;

class MiScaleFinder {
  static class TestScale {
    int f_Key;
    int f_ScaleSize;
    int[] f_Sequence;
  }

  /** The Logger for this class. */
  public static final transient Logger LOG = Logger
      .getLogger(MiScaleFinder.class);

  static private TestScale[] buildReferenceSequences(int inLoPitch,
      int inHiPitch, int inScaleIndex) {
    ScaleManager scaleMgr = TuxGuitar.instance().getScaleManager();
    int[] model = scaleDefToModel(scaleMgr.getScaleKeys(inScaleIndex));
    int[] intervals = scaleModelToIntervals(model);
    TestScale[] sequences = new TestScale[12];

    // LOG.debug();
    // LOG.debug("Scale: " + scaleMgr.getScaleName(inScaleIndex));
    // LOG.debug("Lowest Pitch: " + inLoPitch);
    // LOG.debug("Highest Pitch: " + inHiPitch);
    // LOG.debug("Model: " + Arrays.toString(model));
    // LOG.debug("Intervals: " + Arrays.toString(intervals));

    // build sequences, backwards, one per key
    for (int key = 0; key < 12; key++) {
      // compute sequence length
      int sequenceLength = 0;

      for (int pitch = inLoPitch - key, intervalsIndex = 0; pitch <= inHiPitch;) {
        sequenceLength++;
        pitch += intervals[intervalsIndex];
        intervalsIndex = (intervalsIndex + 1 >= intervals.length ? 0
            : intervalsIndex + 1);
      }

      // initialize sequence
      sequences[key] = new TestScale();
      sequences[key].f_Key = (inLoPitch - key) % 12;
      sequences[key].f_ScaleSize = model.length;
      sequences[key].f_Sequence = new int[sequenceLength];

      // fill sequence
      for (int pitch = inLoPitch - key, intervalsIndex = 0, i = 0; pitch <= inHiPitch;) {
        sequences[key].f_Sequence[i++] = pitch;
        pitch += intervals[intervalsIndex];
        intervalsIndex = (intervalsIndex + 1 >= intervals.length ? 0
            : intervalsIndex + 1);
      }

      // LOG.debug("key: " + key + ", sequence: " +
      // Arrays.toString(sequences[key].f_Sequence));
    }

    return (sequences);
  }

  static private int countMatches(SortedSet<Byte> inScale, int[] inRefSequence) {
    int count = 0;
    
    for (final Byte pitch : inScale) {
      boolean found = false;

      for (int i = 0; i < inRefSequence.length && !found; i++)
        if (pitch == inRefSequence[i])
          found = true;

      if (!found)
        return 0;
      else
        count++;
    }

    return count;
  }

  static public int findMatchingScale(SortedSet<Byte> inScale) {
    ScaleManager scaleMgr = TuxGuitar.instance().getScaleManager();
    int scalesCount = scaleMgr.countScales(), minScaleSize = 12, maxMatches = 0, scaleIndex = ScaleManager.NONE_SELECTION, scaleKey = 0;

    if (!inScale.isEmpty()) {
      int loPitch = ((Byte) inScale.first()).intValue(), hiPitch = ((Byte) inScale
          .last()).intValue();

      // LOG.debug("Input: " + inScale);
      // LOG.debug("loPitch: " + loPitch);
      // LOG.debug("hiPitch: " + hiPitch);

      for (int s = 0; s < scalesCount; s++) {
        TestScale[] refSequences = buildReferenceSequences(loPitch, hiPitch, s);

        for (int key = 0; key < 12; key++) {
          int matches = countMatches(inScale, refSequences[key].f_Sequence);

          if (matches > maxMatches) {
            maxMatches = matches;
            scaleIndex = s;
            scaleKey = refSequences[key].f_Key;
            minScaleSize = refSequences[key].f_ScaleSize;

            // LOG.debug();
            // LOG.debug("more matches: " + scaleMgr.getScaleName(scaleIndex));
            // LOG.debug("maxMatches: " + maxMatches + " minScaleSize: " +
            // minScaleSize);
          } else if (maxMatches > 0 && matches == maxMatches
              && refSequences[key].f_ScaleSize < minScaleSize) {
            maxMatches = matches;
            scaleIndex = s;
            scaleKey = refSequences[key].f_Key;
            minScaleSize = refSequences[key].f_ScaleSize;

            // LOG.debug("");
            // LOG.debug("smaller scale: " + scaleMgr.getScaleName(scaleIndex));
            // LOG.debug("maxMatches: " + maxMatches + " minScaleSize: " +
            // minScaleSize);
          }
        }
      }
    }

    selectScale(scaleIndex, scaleKey);
    return (scaleIndex);
  }

  static private int[] scaleDefToModel(String inScaleDefinition) {
    String[] keys = inScaleDefinition.split(",");
    int[] model = new int[keys.length];

    for (int i = 0; i < keys.length; i++)
      model[i] = (Integer.parseInt(keys[i]) - 1);

    return (model);
  }

  static private int[] scaleModelToIntervals(int[] inModel) {
    int[] intervals = new int[inModel.length];

    for (int i = 1; i < inModel.length; i++)
      intervals[i - 1] = inModel[i] - inModel[i - 1];

    intervals[inModel.length - 1] = 12 - inModel[inModel.length - 1];

    return (intervals);
  }

  static public void selectScale(final int inIndex, final int inKey) {
    try {
      TGSynchronizer.instance().addRunnable(new TGSynchronizer.TGRunnable() {
        public void run() throws Throwable {
          TuxGuitar.instance().getScaleManager().selectScale(inIndex, inKey);
        }
      });
    } catch (Throwable e) {
      LOG.error(e);
    }
  }
}