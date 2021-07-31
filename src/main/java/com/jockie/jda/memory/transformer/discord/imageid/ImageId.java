package com.jockie.jda.memory.transformer.discord.imageid;

import com.jockie.jda.memory.utility.data.DataConvertUtility;

public class ImageId {
	
    public static ImageId convertToImageId(String avatarId) {
		if(avatarId == null) {
			return new ImageId(false, 0, 0);
		}
		
		boolean animated = avatarId.startsWith("a_");
		if(animated) {
			avatarId = avatarId.substring(2);
		}
				
		if(avatarId.length() != 32) {
			return new ImageId(false, 0, 0);
		}
		
		long[] hexToLong = DataConvertUtility.hexToLong(avatarId.toCharArray());
		return new ImageId(animated, hexToLong[0], hexToLong[1]);
    }
    
    public static String convertToString(ImageId imageId) {
    	return ImageId.convertToString(imageId.animated, imageId.lower, imageId.upper);
    }
    
    public static String convertToString(boolean animated, long lower, long upper) {
		if(lower == 0 && upper == 0) {
			return null;
		}
		
		char[] hex = DataConvertUtility.longToHex(new long[] { lower, upper });
		if(animated) {
			return "a_" + new String(hex);
		}else{
			return new String(hex);
		}
    }
	
	public final boolean animated;
	public final long lower;
	public final long upper;
	
	public ImageId(boolean animated, long lower, long upper) {
		this.animated = animated;
		this.lower = lower;
		this.upper = upper;
	}
}