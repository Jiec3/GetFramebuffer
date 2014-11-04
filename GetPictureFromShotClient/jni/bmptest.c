#include "bmptest.h"


unsigned pider_number;
unsigned char bitmap[limit_width * limit_height * 3 + 1]; //bitmap size
struct bmp_rgb *rgbmap = (struct bmp_rgb *) &bitmap;


void set_background(unsigned char r,unsigned g,unsigned b)
{
 unsigned i = 0;
 while(i < info.image_size)
 {
  bitmap[i++] = b;
  bitmap[i++] = g;
  bitmap[i++] = r;
 }
}
unsigned char blockcode[22][22][3];
void set_blockcode(unsigned w, unsigned h)
{
 int i,j,k,rgb;
 
 // for(i = 0; i < h; i++)
 // {
  // for(j = 0; j < w * 3;)
  // {
   // srand((unsigned) time(NULL));
   // bitmap[j++] = 0x00;  //r
   // bitmap[j++] = 0x00;   //g
   // bitmap[j++] = 0xFF;  //b
  // }
 // }

 
 for(i = 0; i < 20; i++)
 {
  for(j = 0; j < 20; j++)
  {
   blockcode[i][j][0] = rand()%255;
   blockcode[i][j][1] = rand()%255;
   blockcode[i][j][2] = rand()%255;
  }
 }
 for(i = 0; i < h; i++)
 {
  for(j = 0; j < w*3;)
  {
   bitmap[i*w*3+j++] = blockcode[i/20][j/60][0];
   bitmap[i*w*3+j++] = blockcode[i/20][j/60][1];
   bitmap[i*w*3+j++] = blockcode[i/20][j/60][2];
  }
 }
}


void set_pixel(unsigned x,unsigned y,unsigned char r,unsigned g,unsigned b)
{
 if(x >= info.width || y >= info.height) return ;
 unsigned int p = (y * info.width + x) * 3;
 if(p < info.image_size)
 {
	bitmap[p] = b;
	bitmap[++p] = g;
	bitmap[++p] = r;
 }
}

// unsigned get_block4(unsigned x,unsigned y,unsigned * que)
// {
 // unsigned char *vis = (unsigned char *) malloc(info.width * info.height);
 
// }

void init_bmp_header(unsigned w, unsigned h)
{
 magic.magic[0] = 'B';
 magic.magic[1] = 'M';

 header.file_size = w * h * 3 + 54;
 header.creater1 = 0;
 header.creater2 = 0;
 header.offset = 54;
 
 info.header_size = 40;
 info.width = w;
 info.height = h;
 info.nplanes = 1;
 info.bitspp = 3 * 8;
 info.compress_type = 0;
 info.image_size = w * h * 3;
 info.hres = 0;
 info.vres = 0;
 info.ncolors = 100;
 info.nimpcolors = 0;
 
 pider_number = w * h;
}

 

//implementations of bmp i/o functions
void read_bmp_header()  //读取头信息，是基本的，通用
{
 fread(&magic, 1 , 2, stdin);  
 
 fread(&header, 4, 3, stdin);
 
 fread(&info, 4 , 10, stdin);
  
 pider_number = info.image_size / 3;
}


void read_bmp_file()   //读取文件信息，是调用头信息，然后在读入文件fread(buffer, size, count, fp)
{
 read_bmp_header();
 
 fread(bitmap, 1, info.image_size, stdin);
}

void write_bmp_header(FILE *fp)
{
 fwrite(&magic, 1, 2, fp);
 
 fwrite(&header, 4, 3, fp);
 
 fwrite(&info, 4, 10, fp);
 
 //set_background(unsigned char r,unsigned g,unsigned b)
}

void write_bmp_file(FILE *fp)
{
 write_bmp_header(fp);
 fwrite(bitmap, 1, info.image_size, fp);
 fclose(fp);
}


void show_bmp_header()
{
 printf("\n\n");
 printf("bmp header info:\n");
 
 printf("pider_number\t%d\n\n\n",pider_number);
 
 
 printf("magic char:\t\t%c%c\n", magic.magic[0], magic.magic[1]);
 printf("width = %d, height = %d\n",info.width,info.height);
 printf("file size:\t\t%d\n", header.file_size);
 printf("creaters:\t\t%d %d\n", header.creater1, header.creater2);
 printf("offset\t\t\t%d\n", header.offset);
 
 printf("header size:\t\t%d\n", info.header_size);
 //printf("image scale:/t/tw=%d h=%d/n", info.width, info.height);
 printf("color planes:\t\t%d\n", info.nplanes);
 printf("bit per pidel:\t\t%d\n", info.bitspp);
 printf("compress type:\t\t%d\n", info.compress_type);
 printf("image size:\t\t%d\n", info.image_size);
 printf("pidels per meter:\th=%d v=%d\n", info.hres, info.vres);
 printf("color number:\t\t%d\n", info.ncolors);
 printf("important color:\t%d\n", info.nimpcolors);
 
 printf("\n\n");
}

  