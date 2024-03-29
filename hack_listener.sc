var bufLen = 10;

bufLen.postln;
s = Server.default;
//s.boot;

s.waitForBoot({	
	~audioInBuf = Buffer.alloc(s,s.sampleRate*10,1);
	~phs = Bus.control(s,1);
	~lastOnset = 0;
	~segments = [];
	
	SynthDef(\hack_listener, {
		
		var in, in_fft, onsets, hainesworth, jensen, pitch, hasPitch, phs;
		
		in = SoundIn.ar(0, 0.2);
		in_fft = FFT(LocalBuf(512), in);
		
		onsets = Onsets.kr(in_fft);
		
		//hainesworth = PV_HainsworthFoote.ar(in_fft, 1.0, 0.0, 0.9, 0.5);
		//SendReply.ar(hainesworth, \hainesworth);
	
		//jensen = PV_JensenAndersen.ar(in_fft, threshold:0.05);
		//SendReply.ar(jensen, \jensen);
		
		# pitch, hasPitch = Pitch.kr(in,median:9,clar:1); // the # means its an array (here with 2 elements)
		//hasPitch.poll;
		hasPitch = hasPitch > 0.4; // with 'clar' on, 0 < hasPitch < 1, so this turns it into a boolean for above another value
		//hasPitch.poll;
		SendReply.kr(hasPitch,\onsets);
		hasPitch = 1-hasPitch; // watch for drops from 1 to 0 and smooth out the output
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
	}).add;
	
	r.remove;
	r = OSCresponderNode(nil, \pitchOffset, {
		arg time, responder, msg;
		~newBuf.(time - ~lastOnset);
	}).add;
	
	~newBuf = {
		arg length;
		var buf, wrapStart, len1, len2;
		length = length * s.sampleRate;
		~phs.get({
			| phs |
			phs.postln;
			if(length > (s.sampleRate*0.5),{
				buf = Buffer.alloc(s,length);
				if(phs-length < 0,{
					//"wrap around".postln;
					wrapStart = phs-length + (s.sampleRate*10);
					len1 = (10*s.sampleRate) - wrapStart;
					len2 = length - len1;
					~audioInBuf.copyData(buf,0,wrapStart,len1);				~audioInBuf.copyData(buf,len1,0,len2);
					//buf.plot;
					~bufManage.(buf);
				},{
					~audioInBuf.copyData(buf,0,phs-length,length);
					//buf.plot;
					~bufManage.(buf);
				});
			});
		});
	};
	
	~bufManage = {
		arg buf;
		if(~segments.size > 49,{
			~segments.removeAllSuchThat({
				arg item, i;
				i == 0;
			});
		});
		~segments = ~segments ++ [Segment(buf)]; 
		~segments.size.postln;
	};
	
	~win = SCWindow("Start Listening",Rect(200,100,400,400))
		.front;
	SCButton(~win,Rect(5,5,390,100))
		.states_([["Start Listening (Only Click Once)"]])
		.action_({Synth(\hack_listener,[\inBuf,~audioInBuf]);});
	SCButton(~win,Rect(5,110,390,100))
		.states_([["Play a Buffer"]])
		.action_({
			if(~segments.size > 0,{
				~segments.choose.buf.play;
			})
		});
}); // close of the s.waitForBoot;

//~player = Task({
//	loop {
//		~segments.choose[0].play;
//		"played".postln;
//		rrand(5,10).wait;
//	};
//});