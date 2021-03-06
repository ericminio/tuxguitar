package org.herac.tuxguitar.player.base;

public class MidiInstrument {

  public static final MidiInstrument[] INSTRUMENT_LIST = new MidiInstrument[] {
      new MidiInstrument("Piano"), new MidiInstrument("Bright Piano"),
      new MidiInstrument("Electric Grand"),
      new MidiInstrument("Honky Tonk Piano"),
      new MidiInstrument("Electric Piano 1"),
      new MidiInstrument("Electric Piano 2"),
      new MidiInstrument("Harpsichord"), new MidiInstrument("Clavinet"),
      new MidiInstrument("Celesta"), new MidiInstrument("Glockenspiel"),
      new MidiInstrument("Music Box"), new MidiInstrument("Vibraphone"),
      new MidiInstrument("Marimba"), new MidiInstrument("Xylophone"),
      new MidiInstrument("Tubular Bell"), new MidiInstrument("Dulcimer"),
      new MidiInstrument("Hammond Organ"), new MidiInstrument("Perc Organ"),
      new MidiInstrument("Rock Organ"), new MidiInstrument("Church Organ"),
      new MidiInstrument("Reed Organ"), new MidiInstrument("Accordion"),
      new MidiInstrument("Harmonica"), new MidiInstrument("Tango Accordion"),
      new MidiInstrument("Nylon Str Guitar"),
      new MidiInstrument("Steel String Guitar"),
      new MidiInstrument("Jazz Electric Gtr"),
      new MidiInstrument("Clean Guitar"), new MidiInstrument("Muted Guitar"),
      new MidiInstrument("Overdrive Guitar"),
      new MidiInstrument("Distortion Guitar"),
      new MidiInstrument("Guitar Harmonics"),
      new MidiInstrument("Acoustic Bass"), new MidiInstrument("Fingered Bass"),
      new MidiInstrument("Picked Bass"), new MidiInstrument("Fretless Bass"),
      new MidiInstrument("Slap Bass 1"), new MidiInstrument("Slap Bass 2"),
      new MidiInstrument("Syn Bass 1"), new MidiInstrument("Syn Bass 2"),
      new MidiInstrument("Violin"), new MidiInstrument("Viola"),
      new MidiInstrument("Cello"), new MidiInstrument("Contrabass"),
      new MidiInstrument("Tremolo Strings"),
      new MidiInstrument("Pizzicato Strings"),
      new MidiInstrument("Orchestral Harp"), new MidiInstrument("Timpani"),
      new MidiInstrument("Ensemble Strings"),
      new MidiInstrument("Slow Strings"),
      new MidiInstrument("Synth Strings 1"),
      new MidiInstrument("Synth Strings 2"), new MidiInstrument("Choir Aahs"),
      new MidiInstrument("Voice Oohs"), new MidiInstrument("Syn Choir"),
      new MidiInstrument("Orchestra Hit"), new MidiInstrument("Trumpet"),
      new MidiInstrument("Trombone"), new MidiInstrument("Tuba"),
      new MidiInstrument("Muted Trumpet"), new MidiInstrument("French Horn"),
      new MidiInstrument("Brass Ensemble"), new MidiInstrument("Syn Brass 1"),
      new MidiInstrument("Syn Brass 2"), new MidiInstrument("Soprano Sax"),
      new MidiInstrument("Alto Sax"), new MidiInstrument("Tenor Sax"),
      new MidiInstrument("Baritone Sax"), new MidiInstrument("Oboe"),
      new MidiInstrument("English Horn"), new MidiInstrument("Bassoon"),
      new MidiInstrument("Clarinet"), new MidiInstrument("Piccolo"),
      new MidiInstrument("Flute"), new MidiInstrument("Recorder"),
      new MidiInstrument("Pan Flute"), new MidiInstrument("Bottle Blow"),
      new MidiInstrument("Shakuhachi"), new MidiInstrument("Whistle"),
      new MidiInstrument("Ocarina"), new MidiInstrument("Syn Square Wave"),
      new MidiInstrument("Syn Saw Wave"), new MidiInstrument("Syn Calliope"),
      new MidiInstrument("Syn Chiff"), new MidiInstrument("Syn Charang"),
      new MidiInstrument("Syn Voice"), new MidiInstrument("Syn Fifths Saw"),
      new MidiInstrument("Syn Brass and Lead"), new MidiInstrument("Fantasia"),
      new MidiInstrument("Warm Pad"), new MidiInstrument("Polysynth"),
      new MidiInstrument("Space Vox"), new MidiInstrument("Bowed Glass"),
      new MidiInstrument("Metal Pad"), new MidiInstrument("Halo Pad"),
      new MidiInstrument("Sweep Pad"), new MidiInstrument("Ice Rain"),
      new MidiInstrument("Soundtrack"), new MidiInstrument("Crystal"),
      new MidiInstrument("Atmosphere"), new MidiInstrument("Brightness"),
      new MidiInstrument("Goblins"), new MidiInstrument("Echo Drops"),
      new MidiInstrument("Sci Fi"), new MidiInstrument("Sitar"),
      new MidiInstrument("Banjo"), new MidiInstrument("Shamisen"),
      new MidiInstrument("Koto"), new MidiInstrument("Kalimba"),
      new MidiInstrument("Bag Pipe"), new MidiInstrument("Fiddle"),
      new MidiInstrument("Shanai"), new MidiInstrument("Tinkle Bell"),
      new MidiInstrument("Agogo"), new MidiInstrument("Steel Drums"),
      new MidiInstrument("Woodblock"), new MidiInstrument("Taiko Drum"),
      new MidiInstrument("Melodic Tom"), new MidiInstrument("Syn Drum"),
      new MidiInstrument("Reverse Cymbal"),
      new MidiInstrument("Guitar Fret Noise"),
      new MidiInstrument("Breath Noise"), new MidiInstrument("Seashore"),
      new MidiInstrument("Bird"), new MidiInstrument("Telephone"),
      new MidiInstrument("Helicopter"), new MidiInstrument("Applause"),
      new MidiInstrument("Gunshot") };

  private String name;

  public MidiInstrument(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}