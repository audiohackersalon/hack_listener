s = Server.default;
//s.boot;

s.waitForBoot({
~audioInBuf = Buffer.alloc(s,s.sampleRate*10,1);
~phs = Bus.control(s,1);
~lastOnset = 0;

SynthDef(\hack_listener, {
	
	var in, in_fft, onsets, hainesworth, jensen, pitch, hasPitch, phs;
	
	in = SoundIn.ar(0, 0.2);
	in_fft = FFT(LocalBuf(512), in);
	
	onsets = Onsets.kr(in_fft);
	SendReply.kr(onsets, \onsets);
	
	hainesworth = PV_HainsworthFoote.ar(in_fft, 1.0, 0.0, 0.9, 0.5);
	SendReply.ar(hainesworth, \hainesworth);

	jensen = PV_JensenAndersen.ar(in_fft, threshold:0.05);
	SendReply.ar(jensen, \jensen);
	
	# pitch, hasPitch = Pitch.kr(in,median:9,clar:1); // the # means its an array (here with 2 elements)
	//hasPitch.poll;
	hasPitch = hasPitch > 0.65; // with 'clar' on, 0 < hasPitch < 1, so this turns it into a boolean for above another value
	//hasPitch.poll;
	hasPitch = (1-hasPitch).lag(0.1); // watch for drops from 1 to 0 and smooth out the output
	SendReply.kr(hasPitch,\pitchOffset);
	
	phs = Phasor.ar(0,1,0,BufFrames.kr(~audioInBuf));
	//phs.poll;
	BufWr.ar(in,~audioInBuf,phs,1);
	Out.kr(~phs,phs);

}).add;

o.remove;
o = OSCresponderNode(nil, \onsets, {
	arg time, responder, msg;
	~lastOnset = time;
	("Onsets onset at " ++ time).postln;
}).add;

//p.remove;
//p = OSCresponderNode(nil, \hainesworth, {
//	arg time, responder, msg;
//	("HainesworthFoote onset at " ++ time).postln;
//}).add;
//
//q.remove;
//q = OSCresponderNode(nil, \jensen, {
//	arg time, responder, msg;
//	("JensenAndersen onset at " ++ time).postln;
//}).add;

r.remove;
r = OSCresponderNode(nil, \pitchOffset, {
	arg time, responder, msg;
	var length; // time since last onset
	("Pitched material ended at " ++ time).postln;
	length = time - ~lastOnset;
	("Length since last onset: " ++ length).postln;
	~newBuf.(length);
}).add;

~newBuf = {
	arg length;
	var buf, wrapStart, len1, len2;
	length = length * s.sampleRate;
	buf = Buffer.alloc(s,length,1);
	~phs.get({
		| phs |
		phs.postln;
		if(((phs-length) >= 0) && (length > (s.sampleRate*0.5)),{
			~audioInBuf.copyData(buf,0,phs-length,length);
			buf.plot;
		});
		if((phs-length < 0) && (length > (s.sampleRate*0.5)),{
			("wrap around").postln;
			wrapStart = phs-length + s.sampleRate*10;//10 should be replaced with a constant
			len1 = s.sampleRate - wrapStart - 1; //is minus 1 necessary?
			len2 = length - len1;
			~audioInBuf.copyData(buf,0,wrapStart,len1);			~audioInBuf.copyData(buf,len1,0,len2);
			buf.plot;
		})
	});
};

~win = SCWindow("Start Listening",Rect(200,100,400,400))
	.front;
SCButton(~win,Rect(5,5,390,390))
	.states_([["Start Listening (Only Click Once)"]])
	.action_({Synth(\hack_listener,[\inBuf,~audioInBuf]);});
}); // close of the s.waitForBoot;