#include <stdio.h>
#include <time.h>

#ifndef _LEARN_H_
#define _LEARN_H_

// 2byte
struct bmp_magic
{
 unsigned char magic[2];
}magic;


// 4 * 3 = 12 Byte
struct bmp_header
{
 unsigned file_size;    //file size in Byte ,w * h * 3 + 54
 unsigned short creater1;   //0
 unsigned short creater2;   //0
 unsigned offset;   //offset to image data: 54D, 36H
}header;

//10 * 4 =  40 Byte
struct bmp_info
{
 unsigned header_size; //info size in bytes, equals 4o D, or 28H
 unsigned width;     //file wideth in pide
 unsigned height;    //file height in pide

 unsigned short nplanes;   //number of clor planes , 1
 unsigned short bitspp;    //bits per pidel, 24d, 18h

 unsigned compress_type;  //compress type,default 0
 unsigned image_size;   //image size in Byte.  w * h * 3
 unsigned hres;      //pideles per meter, 0
 unsigned vres;      //pideles per meter, 0
 unsigned ncolors;   //number of colors, 0
 unsigned nimpcolors; //important colors, 0
}info;

struct bmp_rgb
{
 unsigned char red;
 unsigned char green;
 unsigned char blue;
};

#define WHITH ((rgb) {0xFF, 0xFF, 0xFF})
#define BLACK ((rgb) {0x00, 0x00, 0x00})
#define RED ((rgb) {0xFF, 0x00, 0x00})
#define GREEN ((rgb) {0x00, 0xFF, 0x00})
#define BLUE ((rgb) {0x00, 0x00, 0xFF})

#endif

#define limit_width 4096
#define limit_height 4096

void init_bmp_header(unsigned w, unsigned h); //creat a bmp header
void show_bmp_header();

void read_bmp_header();
void read_bmp_file();

void write_bmp_header(FILE *fp);
void write_bmp_file(FILE *fp);

void set_background(unsigned char r,unsigned g,unsigned b);


void set_pixel(unsigned x,unsigned y,unsigned char r,unsigned g,unsigned b);