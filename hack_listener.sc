SynthDef(\hack_listener, {
	var in = SoundIn.ar(0, 0.2);
	var in_fft = FFT(LocalBuf(512), in);
	var onsets = Onsets.kr(in_fft);
	SendReply.kr(onsets, \onset_chunk);
	// Out.ar(0, );
}).add;

o.remove;
o = OSCresponderNode(nil, \onset_chunk, {
	arg time, responder, msg;	("Onset!!! " ++ [time, responder, msg]).postln;
}).add;

Synth(\hack_listener);