package org.herac.tuxguitar.io.musicxml;

import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.herac.tuxguitar.io.base.TGFileFormatException;
import org.herac.tuxguitar.player.base.MidiInstrument;
import org.herac.tuxguitar.song.managers.TGSongManager;
import org.herac.tuxguitar.song.models.Clef;
import org.herac.tuxguitar.song.models.TGBeat;
import org.herac.tuxguitar.song.models.TGDivisionType;
import org.herac.tuxguitar.song.models.TGDuration;
import org.herac.tuxguitar.song.models.TGMeasure;
import org.herac.tuxguitar.song.models.TGNote;
import org.herac.tuxguitar.song.models.TGSong;
import org.herac.tuxguitar.song.models.TGString;
import org.herac.tuxguitar.song.models.TGTempo;
import org.herac.tuxguitar.song.models.TGTimeSignature;
import org.herac.tuxguitar.song.models.TGTrack;
import org.herac.tuxguitar.song.models.TGVoice;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MusicXMLWriter {

  /** The Logger for this class. */
  public static final transient Logger LOG = Logger
      .getLogger(MusicXMLWriter.class);

  private static class TGVoiceJoiner {
    // private TGFactory factory;
    private TGMeasure measure;

    public TGVoiceJoiner(TGMeasure measure) {
      // this.factory = factory;
      this.measure = measure.clone(measure.getHeader());
      this.measure.setTrack(measure.getTrack());
    }

    public void joinBeats() {
      TGBeat previous = null;
      boolean finish = true;

      long measureStart = this.measure.getStart();
      long measureEnd = (measureStart + this.measure.getLength());
      for (int i = 0; i < this.measure.countBeats(); i++) {
        TGBeat beat = this.measure.getBeat(i);
        TGVoice voice = beat.getVoice(0);
        for (int v = 1; v < beat.countVoices(); v++) {
          TGVoice currentVoice = beat.getVoice(v);
          if (!currentVoice.isEmpty()) {
            for (int n = 0; n < currentVoice.getNotes().size(); n++) {
              TGNote note = currentVoice.getNote(n);
              voice.addNote(note);
            }
          }
        }
        if (voice.isEmpty()) {
          this.measure.removeBeat(beat);
          finish = false;
          break;
        }

        long beatStart = beat.getStart();
        if (previous != null) {
          long previousStart = previous.getStart();

          TGDuration previousBestDuration = null;
          for (int v = /* 1 */0; v < previous.countVoices(); v++) {
            TGVoice previousVoice = previous.getVoice(v);
            if (!previousVoice.isEmpty()) {
              long length = previousVoice.getDuration().getTime();
              if ((previousStart + length) <= beatStart) {
                if (previousBestDuration == null
                    || length > previousBestDuration.getTime()) {
                  previousBestDuration = previousVoice.getDuration();
                }
              }
            }
          }

          if (previousBestDuration != null) {
            previous.getVoice(0).setDuration(previousBestDuration.clone());
          } else {
            if (voice.isRestVoice()) {
              this.measure.removeBeat(beat);
              finish = false;
              break;
            }
            TGDuration duration = TGDuration
                .fromTime((beatStart - previousStart));
            previous.getVoice(0).setDuration(duration);
          }
        }

        TGDuration beatBestDuration = null;
        for (int v = /* 1 */0; v < beat.countVoices(); v++) {
          TGVoice currentVoice = beat.getVoice(v);
          if (!currentVoice.isEmpty()) {
            long length = currentVoice.getDuration().getTime();
            if ((beatStart + length) <= measureEnd) {
              if (beatBestDuration == null
                  || length > beatBestDuration.getTime()) {
                beatBestDuration = currentVoice.getDuration();
              }
            }
          }
        }

        if (beatBestDuration == null) {
          if (voice.isRestVoice()) {
            this.measure.removeBeat(beat);
            finish = false;
            break;
          }
          TGDuration duration = TGDuration.fromTime((measureEnd - beatStart));
          voice.setDuration(duration.clone());
        }
        previous = beat;
      }
      if (!finish) {
        joinBeats();
      }
    }

    public void orderBeats() {
      for (int i = 0; i < this.measure.countBeats(); i++) {
        TGBeat minBeat = null;
        for (int j = i; j < this.measure.countBeats(); j++) {
          TGBeat beat = this.measure.getBeat(j);
          if (minBeat == null || beat.getStart() < minBeat.getStart()) {
            minBeat = beat;
          }
        }
        this.measure.moveBeat(i, minBeat);
      }
    }

    public TGMeasure process() {
      this.orderBeats();
      this.joinBeats();
      return this.measure;
    }
  }

  private static final int DURATION_DIVISIONS = (int) TGDuration.QUARTER_TIME;

  private static final String[] DURATION_NAMES = new String[] { "whole",
      "half", "quarter", "eighth", "16th", "32nd", "64th", };

  private static final int[] DURATION_VALUES = new int[] {
      DURATION_DIVISIONS * 4, // WHOLE
      DURATION_DIVISIONS * 2, // HALF
      DURATION_DIVISIONS * 1, // QUARTER
      DURATION_DIVISIONS / 2, // EIGHTH
      DURATION_DIVISIONS / 4, // SIXTEENTH
      DURATION_DIVISIONS / 8, // THIRTY_SECOND
      DURATION_DIVISIONS / 16, // SIXTY_FOURTH
  };

  private static final boolean[] NOTE_ALTERATIONS = new boolean[] { false,
      true, false, true, false, false, true, false, true, false, true, false };

  private static final int NOTE_FLATS[] = new int[] { 0, 1, 1, 2, 2, 3, 4, 4,
      5, 5, 6, 6 };

  private static final String[] NOTE_NAMES = new String[] { "C", "D", "E", "F",
      "G", "A", "B" };

  private static final int NOTE_SHARPS[] = new int[] { 0, 0, 1, 1, 2, 3, 3, 4,
      4, 5, 5, 6 };

  private Document document;

  private TGSongManager manager;

  private OutputStream stream;

  public MusicXMLWriter(OutputStream stream) {
    this.stream = stream;
  }

  private Node addAttribute(Node node, String name, String value) {
    Attr attribute = this.document.createAttribute(name);
    attribute.setNodeValue(value);
    node.getAttributes().setNamedItem(attribute);
    return node;
  }

  private Node addNode(Node parent, String name) {
    Node node = this.document.createElement(name);
    parent.appendChild(node);
    return node;
  }

  private Node addNode(Node parent, String name, String content) {
    Node node = this.addNode(parent, name);
    node.setTextContent(content);
    return node;
  }

  private Document newDocument() {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.newDocument();
      return document;
    } catch (Throwable throwable) {
      LOG.error(throwable);
    }
    return null;
  }

  private void saveDocument() {
    try {
      TransformerFactory xformFactory = TransformerFactory.newInstance();
      Transformer idTransform = xformFactory.newTransformer();
      Source input = new DOMSource(this.document);
      Result output = new StreamResult(this.stream);
      idTransform.setOutputProperty(OutputKeys.INDENT, "yes");
      idTransform.transform(input, output);
    } catch (Throwable throwable) {
      LOG.error(throwable);
    }
  }

  private void writeBeats(Node parent, TGMeasure measure) {
    int ks = measure.getKeySignature();
    int beatCount = measure.countBeats();
    for (int b = 0; b < beatCount; b++) {
      TGBeat beat = measure.getBeat(b);
      TGVoice voice = beat.getVoice(0);
      if (voice.isRestVoice()) {
        Node noteNode = this.addNode(parent, "note");
        this.addNode(noteNode, "rest");
        this.addNode(noteNode, "voice", "1");
        this.writeDuration(noteNode, voice.getDuration());
      } else {
        int noteCount = voice.getNotes().size();
        for (int n = 0; n < noteCount; n++) {
          TGNote note = voice.getNote(n);

          Node noteNode = this.addNode(parent, "note");
          int value = (beat.getMeasure().getTrack().getString(note.getString())
              .getValue() + note.getValue());

          Node pitchNode = this.addNode(noteNode, "pitch");
          this.addNode(pitchNode, "step",
              NOTE_NAMES[(ks <= 7 ? NOTE_SHARPS[value % 12]
                  : NOTE_FLATS[value % 12])]);
          this.addNode(pitchNode, "octave", Integer.toString(value / 12));
          if (NOTE_ALTERATIONS[value % 12]) {
            this.addNode(pitchNode, "alter", (ks <= 7 ? "1" : "-1"));
          }

          Node technicalNode = this.addNode(
              this.addNode(noteNode, "notations"), "technical");
          this
              .addNode(technicalNode, "fret", Integer.toString(note.getValue()));
          this.addNode(technicalNode, "string", Integer.toString(note
              .getString()));

          this.addNode(noteNode, "voice", "1");
          this.writeDuration(noteNode, voice.getDuration());

          if (note.isTiedNote()) {
            this.addAttribute(this.addNode(noteNode, "tie"), "type", "stop");
          }
          if (n > 0) {
            this.addNode(noteNode, "chord");
          }
        }
      }
    }
  }

  private void writeClef(Node parent, Clef clef) {
    Node node = this.addNode(parent, "clef");
    switch (clef) {
    case TREBLE:
      this.addNode(node, "sign", "G");
      this.addNode(node, "line", "2");
      break;
    case BASS:
      this.addNode(node, "sign", "F");
      this.addNode(node, "line", "4");
      break;
    case TENOR:
      this.addNode(node, "sign", "G");
      this.addNode(node, "line", "2");
      break;
    case ALTO:
      this.addNode(node, "sign", "G");
      this.addNode(node, "line", "2");
      break;
    }
  }

  private void writeDirection(Node parent, TGMeasure measure, TGMeasure previous) {
    boolean tempoChanges = (previous == null || measure.getTempo().getValue() != previous
        .getTempo().getValue());

    if (tempoChanges) {
      Node direction = this.addAttribute(this.addNode(parent, "direction"),
          "placement", "above");
      this.writeMeasureTempo(direction, measure.getTempo());
    }
  }

  private void writeDuration(Node parent, TGDuration duration) {
    int index = duration.getIndex();
    if (index >= 0 && index <= 6) {
      int value = (DURATION_VALUES[index] * duration.getDivision().getTimes() / duration
          .getDivision().getEnters());
      if (duration.isDotted()) {
        value += (value / 2);
      } else if (duration.isDoubleDotted()) {
        value += ((value / 4) * 3);
      }

      this.addNode(parent, "duration", Integer.toString(value));
      this.addNode(parent, "type", DURATION_NAMES[index]);

      if (duration.isDotted()) {
        this.addNode(parent, "dot");
      } else if (duration.isDoubleDotted()) {
        this.addNode(parent, "dot");
        this.addNode(parent, "dot");
      }

      if (!duration.getDivision().isEqual(TGDivisionType.NORMAL)) {
        Node divisionType = this.addNode(parent, "time-modification");
        this.addNode(divisionType, "actual-notes", Integer.toString(duration
            .getDivision().getEnters()));
        this.addNode(divisionType, "normal-notes", Integer.toString(duration
            .getDivision().getTimes()));
      }
    }
  }

  private void writeHeaders(Node parent) {
    this.writeWork(parent);
    this.writeIdentification(parent);
  }

  private void writeIdentification(Node parent) {
    Node identification = this.addNode(parent, "identification");
    this.addNode(this.addNode(identification, "encoding"), "software",
        "TuxGuitar");
    this.addAttribute(this.addNode(identification, "creator", this.manager
        .getSong().getAuthor()), "type", "composer");
  }

  private void writeKeySignature(Node parent, int ks) {
    int value = ks;
    if (value != 0) {
      value = ((((ks - 1) % 7) + 1) * (ks > 7 ? -1 : 1));
    }
    Node key = this.addNode(parent, "key");
    this.addNode(key, "fifths", Integer.toString(value));
    this.addNode(key, "mode", "major");
  }

  private void writeMeasureAttributes(Node parent, TGMeasure measure,
      TGMeasure previous) {
    boolean divisionChanges = (previous == null);
    boolean keyChanges = (previous == null || measure.getKeySignature() != previous
        .getKeySignature());
    boolean clefChanges = (previous == null || measure.getClef() != previous
        .getClef());
    boolean timeSignatureChanges = (previous == null || !measure
        .getTimeSignature().isEqual(previous.getTimeSignature()));
    boolean tuningChanges = (measure.getNumber() == 1);
    if (divisionChanges || keyChanges || clefChanges || timeSignatureChanges) {
      Node measureAttributes = this.addNode(parent, "attributes");
      if (divisionChanges) {
        this.addNode(measureAttributes, "divisions", Integer
            .toString(DURATION_DIVISIONS));
      }
      if (keyChanges) {
        this.writeKeySignature(measureAttributes, measure.getKeySignature());
      }
      if (clefChanges) {
        this.writeClef(measureAttributes, measure.getClef());
      }
      if (timeSignatureChanges) {
        this.writeTimeSignature(measureAttributes, measure.getTimeSignature());
      }
      if (tuningChanges) {
        this.writeTuning(measureAttributes, measure.getTrack());
      }
    }
  }

  private void writeMeasureTempo(Node parent, TGTempo tempo) {
    this.addAttribute(this.addNode(parent, "sound"), "tempo", Integer
        .toString(tempo.getValue()));
  }

  private void writePartList(Node parent) {
    Node partList = this.addNode(parent, "part-list");

    for (final TGTrack track : this.manager.getSong().getTracks()) {

      Node scoreParts = this.addNode(partList, "score-part");
      this.addAttribute(scoreParts, "id", "P" + track.getNumber());

      this.addNode(scoreParts, "part-name", track.getName());

      Node scoreInstrument = this.addAttribute(this.addNode(scoreParts,
          "score-instrument"), "id", "P" + track.getNumber() + "-I1");
      this.addNode(scoreInstrument, "instrument-name",
          MidiInstrument.INSTRUMENT_LIST[track.getChannel().getInstrument()]
              .getName());

      Node midiInstrument = this.addAttribute(this.addNode(scoreParts,
          "midi-instrument"), "id", "P" + track.getNumber() + "-I1");
      this.addNode(midiInstrument, "midi-channel", Integer.toString(track
          .getChannel().getChannel() + 1));
      this.addNode(midiInstrument, "midi-program", Integer.toString(track
          .getChannel().getInstrument() + 1));
    }
  }

  private void writeParts(Node parent) {
    for (final TGTrack track : this.manager.getSong().getTracks()) {
      Node part = this.addAttribute(this.addNode(parent, "part"), "id", "P"
          + track.getNumber());

      TGMeasure previous = null;

      for (final TGMeasure srcMeasure : track.getMeasures()) {
        // TODO: Add multivoice support.
        TGMeasure measure = new TGVoiceJoiner(srcMeasure).process();
        Node measureNode = this.addAttribute(this.addNode(part, "measure"),
            "number", Integer.toString(measure.getNumber()));

        this.writeMeasureAttributes(measureNode, measure, previous);
        this.writeDirection(measureNode, measure, previous);
        this.writeBeats(measureNode, measure);

        previous = measure;
      }
    }
  }

  private void writeSong(Node parent) {
    this.writePartList(parent);
    this.writeParts(parent);
  }

  public void writeSong(TGSong song) throws TGFileFormatException {
    try {
      this.manager = new TGSongManager();
      this.manager.setSong(song);
      this.document = newDocument();

      Node node = this.addNode(this.document, "score-partwise");
      this.writeHeaders(node);
      this.writeSong(node);
      this.saveDocument();

      this.stream.flush();
      this.stream.close();
    } catch (Throwable throwable) {
      throw new TGFileFormatException("Could not write song!.", throwable);
    }
  }

  private void writeTimeSignature(Node parent, TGTimeSignature ts) {
    Node node = this.addNode(parent, "time");
    this.addNode(node, "beats", Integer.toString(ts.getNumerator()));
    this.addNode(node, "beat-type", Integer.toString(ts.getDenominator()
        .getValue()));
  }

  private void writeTuning(Node parent, TGTrack track) {
    Node staffDetailsNode = this.addNode(parent, "staff-details");
    this.addNode(staffDetailsNode, "staff-lines", Integer.toString(track
        .stringCount()));
    for (int i = track.stringCount(); i > 0; i--) {
      TGString string = track.getString(i);
      Node stringNode = this.addNode(staffDetailsNode, "staff-tuning");
      this.addAttribute(stringNode, "line", Integer.toString((track
          .stringCount() - string.getNumber()) + 1));
      this.addNode(stringNode, "tuning-step", NOTE_NAMES[NOTE_SHARPS[(string
          .getValue() % 12)]]);
      this.addNode(stringNode, "tuning-octave", Integer.toString(string
          .getValue() / 12));
    }
  }

  private void writeWork(Node parent) {
    this.addNode(this.addNode(parent, "work"), "work-title", this.manager
        .getSong().getName());
  }

}
