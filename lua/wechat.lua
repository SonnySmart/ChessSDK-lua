
local PLATFORM = cc.Application:getInstance():getTargetPlatform()
local ANDROID = cc.PLATFORM_OS_ANDROID
local IPHONE  = cc.PLATFORM_OS_IPHONE

local wechat = class('wechat')

local activityClassName = "org/cocos2dx/lua/ChessActivity"

function wechat:sendLogin()
	if PLATFORM == ANDROID then
		
	elseif PLATFORM == IPHONE then
	
	else
		print('onClick sendLogin')
	end
end

function wechat:onCallback()

end

return wechat