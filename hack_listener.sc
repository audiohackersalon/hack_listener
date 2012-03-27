SynthDef(\hack_listener, {
	var in, in_fft, onsets, hainesworth, jensen;
	
	in = SoundIn.ar(0, 0.2);
	in_fft = FFT(LocalBuf(512), in);
	
	onsets = Onsets.kr(in_fft);
	SendReply.kr(onsets, \onsets);
	
	hainesworth = PV_HainsworthFoote.ar(in_fft, 1.0, 0.0, 0.9, 0.5);
	SendReply.ar(hainesworth, \hainesworth);

	jensen = PV_JensenAndersen.ar(in_fft, threshold:0.05);
	SendReply.ar(jensen, \jensen);
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

Synth(\hack_listener);