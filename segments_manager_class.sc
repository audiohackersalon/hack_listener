SegmentsManager {
	var <>maxSegments=50, <>segments=[];
	
	// Adds a segment to the array
	*add_segment {
		if segments.length > maxSegments
			cull_least_relevant
		end
		// ... add the segment ....
	}
	
	// Culls the least relevant segment
	*cull_least_relevant {
	}
}