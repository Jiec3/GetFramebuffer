#include <android/log.h>
#define LOG_TAG "debug"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)

/********************************************************************
	    created:    2012/02/07 
	    filename:   myfb.c 
	    author:      

	    purpose:     
 *********************************************************************/
#ifndef WIN32
//-------------------------------------------------------------------

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <linux/fb.h>
#include <linux/kd.h>

#include <memory.h>
#include <jni.h>

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

struct FB
{
	unsigned short *bits;
	unsigned size;
	int fd;
	struct fb_fix_screeninfo fi;
	struct fb_var_screeninfo vi;
};

int fb_bpp(struct FB *fb)
{
	if (fb)
	{
		return fb->vi.bits_per_pixel;
	}
	return 0;
}

int fb_width(struct FB *fb)
{
	if (fb)
	{
		return fb->vi.xres;
	}
	return 0;
}

int fb_height(struct FB *fb)
{
	if (fb)
	{
		return fb->vi.yres;
	}
	return 0;
}

int fb_size(struct FB *fb)
{
	if (fb)
	{
		unsigned bytespp = fb->vi.bits_per_pixel / 8;
		return (fb->vi.xres * fb->vi.yres * bytespp);
	}
	return 0;
}

int fb_virtual_size(struct FB *fb)
{
	if (fb)
	{
		unsigned bytespp = fb->vi.bits_per_pixel / 8;
		return (fb->vi.xres_virtual * fb->vi.yres_virtual * bytespp);
	}
	return 0;
}

void * fb_bits(struct FB *fb)
{
	unsigned short * bits = NULL;
	if (fb)
	{
		int offset, bytespp;
		bytespp = fb->vi.bits_per_pixel / 8;

		/* HACK: for several of our 3d cores a specific alignment
		 * is required so the start of the fb may not be an integer number of lines
		 * from the base.  As a result we are storing the additional offset in
		 * xoffset. This is not the correct usage for xoffset, it should be added
		 * to each line, not just once at the beginning */
		offset = fb->vi.xoffset * bytespp;
		offset += fb->vi.xres_virtual * fb->vi.yoffset * bytespp;
		bits = fb->bits + offset / sizeof(*fb->bits);
	}
	return bits;
}

void fb_update(struct FB *fb)
{
	if (fb)
	{
		fb->vi.yoffset = 1;
		ioctl(fb->fd, FBIOPUT_VSCREENINFO, &fb->vi);
		fb->vi.yoffset = 0;
		ioctl(fb->fd, FBIOPUT_VSCREENINFO, &fb->vi);
	}
}

int fb_map_size(struct FB *fb)
{
	int offset = (fb->vi.xoffset + fb->vi.yoffset*fb->vi.xres) * 3;
	int w = fb->vi.xres;
	int h = fb->vi.yres;
	LOGI("offset x = %d y = %d size = %d", fb->vi.xoffset, fb->vi.yoffset, offset);
	LOGI("w = %d, h = %d", w, h);
	int size = w*h*3;
	int mapsize = offset + size;

	return mapsize;
}

static int fb_open(struct FB *fb)
{
	if (NULL == fb)
	{
		return -1;
	}

	fb->fd = open("/dev/graphics/fb0", O_RDONLY | O_RDWR);
	if (fb->fd < 0)
	{
		printf("open(\"/dev/graphics/fb0\") failed!\n");
		LOGI("---open(\"/dev/graphics/fb0\") failed!---");
		return -1;
	}

	struct fb_var_screeninfo vi;
	vi.xres = 1088;
	vi.yres = 1800;
	vi.xres_virtual = 1088;
	vi.yres_virtual = 4000;

	ioctl(fb->fd, FBIOPUT_VSCREENINFO, vi);


	if (ioctl(fb->fd, FBIOGET_FSCREENINFO, &fb->fi) < 0)
	{
		printf("FBIOGET_FSCREENINFO failed!\n");
		LOGI("---FBIOGET_FSCREENINFO failed!---");
		goto fail;
	}

	LOGD("====== smem_start : %lu",  fb->fi.smem_start);
	// Framebuffer设备的大小
	LOGD("====== smem_len : %d",  fb->fi.smem_len);
	// 一行的byte数目 除以 (bits_per_pixel/8) 就是一行的像素点的数目
	LOGD("====== line_length : %d",  fb->fi.line_length);

	//FB_TYPE_PACKED_PIXELS                0       /* Packed Pixels        */
	//FB_TYPE_PLANES                            1       /* Non interleaved planes */
	//FB_TYPE_INTERLEAVED_PLANES      2       /* Interleaved planes   */
	//FB_TYPE_TEXT                                3       /* Text/attributes      */
	//FB_TYPE_VGA_PLANES                    4       /* EGA/VGA planes       */
	//FB_TYPE_FOURCC                          5       /* Type identified by a V4L2 FOURCC */
	LOGD("====== type : %d",  fb->fi.type);

	if (ioctl(fb->fd, FBIOGET_VSCREENINFO, &fb->vi) < 0)
	{
		printf("FBIOGET_VSCREENINFO failed!\n");
		LOGI("---FBIOGET_VSCREENINFO failed!---");
		goto fail;
	}



	/*打印信息*/
	{
		// 这两个是显示在显示屏上时候的分辨率
		LOGD("====== xres : %d",  fb->vi.xres);
		LOGD("====== yres : %d",  fb->vi.yres);
		// 这两个是显存缓存的分辨率 如果显存缓存了两个屏幕的时候
		// yres_virtula 应该等于 yres * 2
		// 而 xres_virtual 就应该 == xres
		LOGD("====== xres_virtual : %d",  fb->vi.xres_virtual);
		LOGD("====== yres_virtual : %d",  fb->vi.yres_virtual);

		/* offset from virtual to visible */
		// 显存可能缓存了多个屏幕，哪到底哪个屏幕才是显示屏应该显示的内容呢
		// 这就是由下面这两个offset来决定了
		LOGD("====== xoffset : %d",  fb->vi.xoffset);
		LOGD("====== yoffset : %d",  fb->vi.yoffset);

		LOGD("====== bits_per_pixel : %d",  fb->vi.bits_per_pixel);

		// 下面这一段是每个像素点的格式
		LOGD("====== fb_bitfield red.offset : %d",  fb->vi.red.offset);
		LOGD("====== fb_bitfield red.length : %d",  fb->vi.red.length);
		// 如果 == 0，指的是数据的最高有效位在最左边 也就是Big endian
		LOGD("====== fb_bitfield red.msb_right : %d",  fb->vi.red.msb_right);
		LOGD("====== fb_bitfield green.offset : %d",  fb->vi.green.offset);
		LOGD("====== fb_bitfield green.length : %d",  fb->vi.green.length);
		LOGD("====== fb_bitfield green.msb_right : %d",  fb->vi.green.msb_right);
		LOGD("====== fb_bitfield blue.offset : %d",  fb->vi.blue.offset);
		LOGD("====== fb_bitfield blue.length : %d",  fb->vi.blue.length);
		LOGD("====== fb_bitfield blue.msb_right : %d",  fb->vi.blue.msb_right);
		LOGD("====== fb_bitfield transp.offset : %d",  fb->vi.transp.offset);
		LOGD("====== fb_bitfield transp.length : %d",  fb->vi.transp.length);
		LOGD("====== fb_bitfield transp.msb_right : %d",  fb->vi.transp.msb_right);

		LOGD("====== height : %d",  fb->vi.height);
		// width of picture in mm 毫米
		LOGD("====== width : %d",  fb->vi.width);
		// UP 0
		// CW 1
		// UD 2
		// CCW 3
		/* angle we rotate counter clockwise */
		LOGD("====== rotate : %d",  fb->vi.rotate);
	}

	fb->bits = mmap(0, fb_virtual_size(fb), PROT_READ, MAP_SHARED, fb->fd, 0);


	if (fb->bits == MAP_FAILED) {
		printf("mmap() failed!\n");
		LOGI("---mmap()失败！---");
		goto fail;
	}

	return 0;

	fail:
	LOGI("---fb_open()失败！---");
	close(fb->fd);
	return -1;
}

static void fb_close(struct FB *fb)
{
	if (fb)
	{
		munmap(fb->bits, fb_virtual_size(fb));
		close(fb->fd);
	}
}

static struct FB g_fb;
int fb_create(void)
{
	memset(&g_fb, 0, sizeof(struct FB));
	if (fb_open(&g_fb))
	{
		return -1;
	}
	return 0;
}

void fb_destory(struct FB *fb)
{
	fb_close(fb);
}
//-------------------------------------------------------------------
//-------------------------------------------------------------------

int savePic(const char *filePath)
{
	int w = g_fb.vi.xres_virtual, h = g_fb.vi.yres, depth = g_fb.vi.bits_per_pixel;

	//convert pixel data
	uint8_t *rgb24;
	rgb24 = (uint8_t *) malloc(w * h * 4);
	int i=0;


	for (;i<w*h;i++)
	{
		uint32_t pixel32 = ((uint32_t *)fb_bits(&g_fb))[i];
		// in rgb24 color max is 2^8 per channel
		if (1) {
			rgb24[3*i+0]   = (pixel32 & 0x000000FF); 		//Blue
			rgb24[3*i+1]   = (pixel32 & 0x0000FF00) >> 8;	//Green
			rgb24[3*i+2]   = (pixel32 & 0x00FF0000) >> 16; 	//Red
		} else {
			rgb24[3*i+0]   = (pixel32 & 0x0000FF00) >> 8; 		//Blue
			rgb24[3*i+1]   = (pixel32 & 0x00FF0000) >> 16;	//Green
			rgb24[3*i+2]   = (pixel32 & 0xFF000000) >> 24; 	//Red
		}
	}

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
	bmpfile = fopen(filePath, "wb");
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
	int line,x;
	for (line = h-1; line >= 0; line --)
	{
		// fill line linebuf with the image data for that line
		for( x =0 ; x < w; x++ )
		{
			if (g_fb.vi.blue.offset == 0)
			{
				*(linebuf+x*bytes_per_pixel) = *(rgb24 + (x+line*w)*bytes_per_pixel+0);
				*(linebuf+x*bytes_per_pixel+1) = *(rgb24 + (x+line*w)*bytes_per_pixel+1);
				*(linebuf+x*bytes_per_pixel+2) = *(rgb24 + (x+line*w)*bytes_per_pixel+2);
			} else
			{
				*(linebuf+x*bytes_per_pixel) = *(rgb24 + (x+line*w)*bytes_per_pixel+2);
				*(linebuf+x*bytes_per_pixel+1) = *(rgb24 + (x+line*w)*bytes_per_pixel+1);
				*(linebuf+x*bytes_per_pixel+2) = *(rgb24 + (x+line*w)*bytes_per_pixel);
			}

		}
		// remember that the order is BGR and if width is not a multiple
		// of 4 then the last few bytes may be unused
		fwrite(linebuf, 1, bytesPerLine, bmpfile);
	}
	int pointx = 400, pointy = 400;
	LOGI("color r = %d, g = %d, b = %d",
			*(rgb24 + (pointx+pointy*w)*bytes_per_pixel+0),
			*(rgb24 + (pointx+pointy*w)*bytes_per_pixel+1),
			*(rgb24 + (pointx+pointy*w)*bytes_per_pixel+2));
	fclose(bmpfile);
}

int getColor(int pointx, int pointy)
{
	int w = g_fb.vi.xres_virtual, h = g_fb.vi.yres, depth = g_fb.vi.bits_per_pixel;
	int r, g, b;

	uint32_t pixel32 = ((uint32_t *)fb_bits(&g_fb))[(pointy - 1) * g_fb.vi.xres + pointx];

	if (g_fb.vi.blue.offset == 0) {
		//BGRA
		b   = (pixel32 & 0x000000FF); 		//Blue
		g   = (pixel32 & 0x0000FF00) >> 8;	//Green
		r   = (pixel32 & 0x00FF0000) >> 16; 	//Red
	} else {
		//ABGR
		b   = (pixel32 & 0x0000FF00) >> 8; 		//Blue
		g   = (pixel32 & 0x00FF0000) >> 16;	//Green
		r   = (pixel32 & 0xFF000000) >> 24; 	//Red
	}

	//返回到java层的时候转换成统一的BGRA顺序格式
	int color = 0xFF000000 + (r << 16) + (g << 8) + b;
	LOGI("color = %d", color);
	LOGI("r = %d, g = %d, b = %d", r, g, b);

	return color;
}

//-------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_com_syouquan_script_ColorEngine_nativeGetColor
(JNIEnv *env, jobject thiz, jint x, jint y)
{
	LOGI("---进入到屏幕截图本地调用函数！---");

	int i = 0;

	i = fb_create();
	if (i == -1)
	{
		LOGI("打开fb0失败");
		return -1;
	}

	int color = getColor(x, y);

	fb_destory(&g_fb);

	return color;
}

JNIEXPORT jint JNICALL Java_com_syouquan_script_ColorEngine_nativeScreenShot
(JNIEnv *env, jobject thiz, jstring path)
{
	LOGI("---进入到屏幕截图本地调用函数！---");

	int i = 0;

	i = fb_create();
	if (i == -1)
	{
		LOGI("打开fb0失败");
		return -1;
	}

	char *filePath = (char*)(*env)->GetStringUTFChars(env, path, NULL);
	i = savePic(filePath);

	fb_destory(&g_fb);

	if(i == -1)
	{
		LOGI("---生成文件失败！---");
		return -1;
	}
	LOGI("---生成文件成功！---");
	return 0;
}

#endif//#ifndef WIN32

