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


extern "C" {
#include "bmptest.h"
}

#define  LOG_TAG    "getPic"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__);

typedef struct {
	long filesize;
	char reserved[2];
	long headersize;
	long infoSize;
	long width;
	long depth;
	short biPlanes;
	short bits;
	long biCompression;
	long biSizeImage;
	long biXPelsPerMeter;
	long biYPelsPerMeter;
	long biClrUsed;
	long biClrImportant;
} BMPHEAD;


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
	
	LOGE("pixels : %s", pixels);
	

	//save RGB 24 BitmapABGR  BGRA
	int bytes_per_pixel = 3;
	BMPHEAD bh;
	memset ((char *)&bh,0,sizeof(BMPHEAD)); // sets everything to 0
	//bh.filesize  =   calculated size of your file (see below)
	//bh.reserved  = two zero bytes
	bh.headersize  = 54L;			// for 24 bit images
	bh.infoSize  =  0x28L;		// for 24 bit images
	bh.width     = w;			// width of image in pixels
	bh.depth     = h;			// height of image in pixels
	bh.biPlanes  =  1;			// for 24 bit images
	bh.bits      = 8 * bytes_per_pixel;	// for 24 bit images
	bh.biCompression = 0L;		// no compression
	int bytesPerLine;
	bytesPerLine = w * bytes_per_pixel;  	// for 24 bit images
	//round up to a dword boundary
	if (bytesPerLine & 0x0003)
	{
		bytesPerLine |= 0x0003;
		++bytesPerLine;
	}
	bh.filesize = bh.headersize + (long)bytesPerLine * bh.depth;
	FILE * bmpfile;
	//printf("Bytes per line : %d\n", bytesPerLine);
	bmpfile = fopen("/sdcard/jiec.bmp", "wb");
	if (bmpfile == NULL)
	{
		LOGI("bmpfile is null");
		return -1;
	}
	fwrite("BM",1,2,bmpfile);
	fwrite((char *)&bh, 1, sizeof (bh), bmpfile);
	//fwrite(rgb24,1,w*h*3,bmpfile);
	char *linebuf;
	linebuf = (char *) calloc(1, bytesPerLine);
	if (linebuf == NULL)
	{
		fclose(bmpfile);
		LOGI("linebuf can calloc");
		return -1;
	}
		
	//convert pixel data
	uint8_t *rgb24;
	rgb24 = (uint8_t *) malloc(w * h * 3);
	i=0;


		
	int line,x;
	for (line = h-1; line >= 0; line --)
	{
		// fill line linebuf with the image data for that line
		for( x =0 ; x < w; x++ )
		{
			unsigned int p = (line * w + x) *4;
			*(linebuf+x*bytes_per_pixel+2) = pixels[p];
			*(linebuf+x*bytes_per_pixel+1) = pixels[++p];
			*(linebuf+x*bytes_per_pixel) = pixels[++p];
		}
		// remember that the order is BGR and if width is not a multiple
		// of 4 then the last few bytes may be unused
		fwrite(linebuf, 1, bytesPerLine, bmpfile);
	}

	fclose(bmpfile);

	client->release();
	return 0;
}
