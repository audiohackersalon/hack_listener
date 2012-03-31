s = Server.default;
s.boot;

SynthDef(\hack_listener, {

	var in, in_fft, onsets, hainesworth, jensen, pitch, hasPitch;
	
	in = SoundIn.ar(0, 0.2);
	in_fft = FFT(LocalBuf(512), in);
	
	onsets = Onsets.kr(in_fft);
	SendReply.kr(onsets, \onsets);
	
	hainesworth = PV_HainsworthFoote.ar(in_fft, 1.0, 0.0, 0.9, 0.5);
	SendReply.ar(hainesworth, \hainesworth);

	jensen = PV_JensenAndersen.ar(in_fft, threshold:0.05);
	SendReply.ar(jensen, \jensen);
	
	# pitch, hasPitch = Pitch.kr(in,median:9,clar:1); // the # means its an array (here with 2 elements)
	hasPitch = hasPitch > 0.9; // with 'clar' on, 0 < hasPitch < 1, so this turns it into a boolean for above 0.9
	hasPitch = (1-hasPitch).lag(0.1); // watch for drops from 1 to 0 and smooth out the output
	SendReply.kr(hasPitch,\pitchOffset);

}).add;

o.remove;
o = OSCresponderNode(nil, \onsets, {
	arg time, responder, msg;
	("Onsets onset at " ++ time).postln;
}).add;

p.remove;
p = OSCresponderNode(nil, \hainesworth, {
	arg time, responder, msg;
	("HainesworthFoote onset at " ++ time).postln;
}).add;

q.remove;
q = OSCresponderNode(nil, \jensen, {
	arg time, responder, msg;
	("JensenAndersen onset at " ++ time).postln;
}).add;

r.remove;
r = OSCresponderNode(nil, \pitchOffset, {
	arg time, responder, msg;
	("Pitched material ended at " ++ time).postln;
}).add;

Synth(\hack_listener);