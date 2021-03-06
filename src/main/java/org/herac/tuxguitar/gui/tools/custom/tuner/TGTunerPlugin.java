package org.herac.tuxguitar.gui.tools.custom.tuner;

import java.util.Iterator;
import java.util.List;

import org.herac.tuxguitar.gui.TuxGuitar;
import org.herac.tuxguitar.song.models.TGString;

/**
 * @author Nikola Kolarovic <nikola.kolarovic at gmail.com>
 * 
 */
public class TGTunerPlugin extends
    org.herac.tuxguitar.gui.system.plugins.base.TGToolItemPlugin {

  protected void doAction() {
    List<TGString> strings = TuxGuitar.instance().getTablatureEditor().getTablature()
        .getCaret().getTrack().getStrings();

    int[] tuning = new int[strings.size()];
    int i = 0;
    
    for (final TGString current : strings) {
      tuning[i] = current.getValue();
      i++;
    }
    TGTunerDialog dialog = new TGTunerDialog(tuning);
    dialog.show();

  }

  public String getAuthor() {
    return "Nikola Kolarovic";
  }

  public String getDescription() {
    return "Visual tuner that analyses the most dominant frequency from your microphone"
        + " and displays it on the tuner scale.";
  }

  protected String getItemName() {
    return "Guitar Tuner";
  }

  public String getName() {
    return "GuitarTuner";
  }

  public String getVersion() {
    return "0.01b";
  }

}
