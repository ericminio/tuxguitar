package org.herac.tuxguitar.io.lilypond;

import org.herac.tuxguitar.gui.system.plugins.base.TGExporterPlugin;
import org.herac.tuxguitar.io.base.TGRawExporter;

public class LilypondPluginExporter extends TGExporterPlugin {

  public String getAuthor() {
    return "Julian Casadesus <julian@casadesus.com.ar>";
  }

  public String getDescription() {
    return "Lilypond 2.10.5 exporter plugin";
  }

  protected TGRawExporter getExporter() {
    return new LilypondSongExporter();
  }

  public String getName() {
    return "Lilypond exporter";
  }

  public String getVersion() {
    return "1.0";
  }
}
