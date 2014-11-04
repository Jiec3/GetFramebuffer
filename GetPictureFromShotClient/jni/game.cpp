#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include <android/log.h>
#include <fcntl.h>
#include <linux/input.h>
#include <string.h>
#include <pthread.h>
#include "SurfaceComposerClient.h"
#include "mp4v2.h"


extern "C" {
#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
#include "bmptest.h"
}

#define  LOG_TAG    "mytest"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__);



int main(int argc,char **argv){
	int i,j,w,h,pix,pb,pr,pg;
	int buff[480*800]={0};
	FILE *fp;
	int once = 1;
	
	android::ScreenshotClient *client = new android::ScreenshotClient();
	

	client->update();
	LOGI("width:%d height:%d size:%d",client->getWidth(),client->getHeight(),client->getSize());
	w = client->getWidth();
	h = client->getHeight();
	const char *pixels = (char*)client->getPixels();
	

	if(once){
		fp = fopen("/sdcard/20141027screen.bmp","wb");
		init_bmp_header(w,h);
		for(i=0;i < h;i++){
			for(j=0;j < w;j++){
				unsigned int p = ((h-i) * w + j) *4;
				pr = pixels[p];
				pg = pixels[++p];
				pb = pixels[++p];
				set_pixel(j,i,pr,pg,pb);
			}
		}
		write_bmp_file(fp);
		LOGI("save file..");
	}


	client->release();
	return 0;
}
