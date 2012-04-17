HackListener {
	var <>lastOnset, <>listeningBus, <>maxSegments=50, segmentsManager=SegmentsManager.new;
	
	// Launches the Synth
	// Uses OSCResponders to track onsets & offsets
	*listen { 
		o = OSCresponderNode(nil, \onsets, {
			arg time, responder, msg;
			this.pitch_onset_respond;
		}).add;

		r = OSCresponderNode(nil, \pitchOffset, {
			arg time, responder, msg;
			this.pitch_end_respond;
		}).add;
	}
	
	
	// replaces ~newBuf
	*store_segment_since { arg bufferStartIndex; 
		
	}
	
	*cull_segments {
		
	}
	
	// Responds to onset of a pitched sound
	// Sets lastOnset.
	*pitch_onset_respond { arg time; 
		lastOnset = time;
		("Onsets onset at " ++ time).postln;
	}
	
	// If the termination of pitched material is useful, stores the segment from lastOnset to current time. 
	*pitch_end_respond { arg time; 
		var length; // time since last onset
		("Pitched material ended at " ++ time).postln;
		length = time - lastOnset;
		("Length since last onset: " ++ length).postln;
		~newBuf.(length);
		this.store_segment_since(lastOnset);
	}

}


listener = HackListener.new
listener.listen




