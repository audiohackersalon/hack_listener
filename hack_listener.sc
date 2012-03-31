s = Server.default;
s.boot;

(
~variables.lastOnset = 0;

SynthDef(\hack_listener, {
	var in = SoundIn.ar(0, 0.2);
	var in_fft = FFT(LocalBuf(2048), in);
	var onsets = Onsets.kr(in_fft);
	var pitch, hasPitch; // variables for pitch detection
	# pitch, hasPitch = Pitch.kr(in,median:9,clar:1); // the # means its an array (here with 2 elements)
	hasPitch = hasPitch > 0.9; // with 'clar' on, 0 < hasPitch < 1, so this turns it into a boolean for above 0.9
	//hasPitch.poll;
	//(1-hasPitch).poll;
	//var hf = PV_HainsworthFoote.ar(in_fft,1,0,0.9,0.5);
	//var ja = PV_JensenAndersen.ar(in_fft,threshold:0.05);
	hasPitch = (1-hasPitch).lag(0.1); // watch for drops from 1 to 0 and smooth out the output
	SendReply.kr(hasPitch,\pitchOffset);
	SendReply.kr(onsets, \onset);
	//SendReply.ar(ja, \ja);
	// Out.ar(0, );
}).add;

o.remove;
o = OSCresponderNode(nil, \onset, {
	arg time, responder, msg;	
	//("onset" ++ [time, responder, msg]).postln;
	"onset".postln;
}).add;

p.remove;
p = OSCresponderNode(nil, \pitchOffset, {
	arg time, responder, msg;	
	//("hf Onset!!! " ++ [time, responder, msg]).postln;
	"pitch offset".postln;
	
}).add;
)

Synth(\hack_listener);

//q.remove;
//q = OSCresponderNode(nil, \ja, {
//	arg time, responder, msg;	
//	//("ja Onset!!! " ++ [time, responder, msg]).postln;
//	"ja".postln;
//}).add;