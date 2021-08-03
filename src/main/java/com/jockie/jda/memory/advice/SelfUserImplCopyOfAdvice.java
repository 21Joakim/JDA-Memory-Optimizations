package com.jockie.jda.memory.advice;

import net.bytebuddy.asm.Advice;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.SelfUserImpl;

public class SelfUserImplCopyOfAdvice {
	
	@Advice.OnMethodEnter(skipOn=Advice.OnNonDefaultValue.class)
	public static SelfUserImpl enter(@Advice.Argument(value=0) SelfUserImpl other, @Advice.Argument(value=1) JDAImpl jda) {
        SelfUserImpl selfUser = new SelfUserImpl(other.getIdLong(), jda);
        selfUser.setName(other.getName())
                .setAvatarId(other.getAvatarId())
                .setDiscriminator(other.getDiscriminator())
                .setBot(other.isBot());
        
        return selfUser
                .setVerified(other.isVerified())
                .setMfaEnabled(other.isMfaEnabled())
                .setApplicationId(other.getApplicationIdLong());
	}
	
	@Advice.OnMethodExit
	public static void exit(@Advice.Enter SelfUserImpl selfUserImpl, @Advice.Return(readOnly=false) SelfUserImpl returnObject) {
		returnObject = selfUserImpl;
	}
}