#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>
#include <errno.h>
#include <linux/unistd.h>
#include <pthread.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <sys/select.h>
#include <sys/time.h>
#include <linux/input.h>
#include <android/log.h>
#include <math.h>
#include <fcntl.h>
#include <sys/epoll.h>


#define LOG_TAG "TEST_SERIAL"
#undef  LOG
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

static int uart_fd;
static int         epoll_fd;

//配置串口参数
int set_opt(int fd, int nSpeed, int nBits, char nEvent, int nStop) {
	struct termios newtio, oldtio;
	if (tcgetattr(fd, &oldtio) != 0) {
		perror("SetupSerial 1");
		return -1;
	}
	bzero(&newtio, sizeof(newtio));
	newtio.c_cflag &= ~CSTOPB;
	        newtio.c_cflag &= ~CSIZE;
	        newtio.c_cflag |= (CLOCAL | CREAD);
	        newtio.c_cflag &= ~CRTSCTS;

	        /* set no software stream control */
	        newtio.c_iflag &= ~(IXON | INLCR | ICRNL | IGNCR | IUCLC);
	        /* set output mode with no define*/
	        newtio.c_oflag &= ~OPOST;
	        /* set input mode with non-format */
	        newtio.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
	        newtio.c_iflag |= IGNBRK|IGNPAR; //for 0xd,0x11,0x13

	switch (nBits) {
	case 7:
		newtio.c_cflag |= CS7;
		break;
	case 8:
		newtio.c_cflag |= CS8;
		break;
	}

	switch (nEvent) {
	case 'O':
		newtio.c_cflag |= PARENB;
	//	newtio.c_cflag |= PARODD;
	//	newtio.c_iflag |= (INPCK | ISTRIP);
		break;
	case 'E':
		newtio.c_iflag |= (INPCK | ISTRIP);
		newtio.c_cflag |= PARENB;
		newtio.c_cflag &= ~PARODD;
		break;
	case 'N':
		newtio.c_cflag &= ~PARENB;
		break;
	}

	switch (nSpeed) {
	case 2400:
		cfsetispeed(&newtio, B2400);
		cfsetospeed(&newtio, B2400);
		break;
	case 4800:
		cfsetispeed(&newtio, B4800);
		cfsetospeed(&newtio, B4800);
		break;
	case 9600:
		cfsetispeed(&newtio, B9600);
		cfsetospeed(&newtio, B9600);
		break;
	case 38400:
		cfsetispeed(&newtio, B38400);
		cfsetospeed(&newtio, B38400);
		break;
	case 115200:
		cfsetispeed(&newtio, B115200);
		cfsetospeed(&newtio, B115200);
		break;
	case 460800:
		cfsetispeed(&newtio, B460800);
		cfsetospeed(&newtio, B460800);
		break;
	default:
		cfsetispeed(&newtio, B9600);
		cfsetospeed(&newtio, B9600);
		break;
	}
	if (nStop == 1)
		newtio.c_cflag &= ~CSTOPB;
	else if (nStop == 2)
		newtio.c_cflag |= CSTOPB;
	newtio.c_cc[VTIME] = 0;
	newtio.c_cc[VMIN] = 0;
	tcflush(fd, TCIFLUSH);
	if ((tcsetattr(fd, TCSANOW, &newtio)) != 0) {
		perror("com set error");
		return -1;
	}
	return 0;
}

/*
wr: '0', 设置rs485模块为写模式
    '1', 设置rs485模块为读模式, 注意是字符串
*/
int control_rs485(char wr){
	int fd = open("/sys/class/io_control/rs485_con", O_RDWR | O_NOCTTY);
	if (fd == -1) {
		return 2;
	}
	write(fd, &wr, 1);
	close(fd);
}

static int read_uart_data(char* data,  int len)
{
	 struct timeval timeout;
	 timeout.tv_sec = 2;
	 timeout.tv_usec = 0;
	 int ret = 0;
	 memset(data,0,len);
     do {
             fd_set readfds;
             FD_ZERO(&readfds);
             FD_SET(uart_fd, &readfds);
             //wait for 2 seconds if no data come
             ret = select(FD_SETSIZE, &readfds, NULL, NULL, &timeout);
             if (ret < 0)
                     continue;
             if (FD_ISSET(uart_fd, &readfds)) {
                     ret = read( uart_fd, data, len);
             }
     } while (ret < 0 && errno == EINTR);
     return ret;
}

int Java_com_ss_testserial_Startwrite_uartInit(JNIEnv* env, jobject thiz) {
	//打开串口
	uart_fd = open("/dev/ttyS3", O_RDWR);
		if (uart_fd == -1) {
			return -1;
		}
		//配置串口
		int nset = set_opt(uart_fd, 9600, 8, 'N', 1);
		if (nset == -1) {
			return -1;
		}


}
int Java_com_ss_testserial_Startwrite_uartDestroy(JNIEnv* env, jobject thiz) {
	close(uart_fd);

}
/**
 * cardID：板子标号 		doorID：门号				 info: 单片机返回的数据
 * return 0表示获取信息成功，-1代表失败
 */
int Java_com_ss_testserial_Startwrite_openGrid(JNIEnv* env, jobject thiz, jint cardID,
		jint doorID, jintArray info) {
	int* buf;
		int i, ret=-1, len,j;

	//协议五位  命令：0x8A   板地址：0X01-0XC8    锁地址：0X01—18    状态：0X11    校检码：前面几位异或
	char xwdata[5] = { 0X8A, (char) cardID, (char) doorID, 0x11 };
	char xrdata[5] = { 0 };
	char xrdata_tmp[50] = {0};

	int start;

	xwdata[4] = (xwdata[0] ^ xwdata[1] ^ xwdata[2] ^ xwdata[3]) & 0xff;
//	LOGD("Open cardId %d door %d\n",cardID,doorID);
	LOGD("open send:0x%x:0x%x:0x%x:0x%x:0x%x\n",xwdata[0],xwdata[1],xwdata[2],xwdata[3],xwdata[4]);
	tcflush(uart_fd,   TCIOFLUSH);
	control_rs485('0');
	   usleep(5000);
		write(uart_fd, xwdata, 5);
		usleep(10000); //必须等待一段时间，rs485才会把数据发送出去
		control_rs485('1');
		ret = read_uart_data(xrdata,5);
		if(ret <= 0) {
			LOGD("Open: fail to get uart data\n");

		}
				if((xrdata[0]==0x00) && (xrdata[1]==0x00)) {
					LOGD("###########open fail. board %d door %d#################\n",cardID,doorID);
				}
		//		LOGD("open ret:0x%x:0x%x:0x%x:0x%x:0x%x\n",xrdata[0],xrdata[1],xrdata[2],xrdata[3],xrdata[4]);


				buf = (int*)(*env)->GetIntArrayElements(env,info,NULL);
				for(i=0;i<5;i++)
					buf[i] = xrdata[i];
		(*env)->ReleaseIntArrayElements(env,info, (jint*)buf, JNI_ABORT);

				return 0;

}
/**
 * 获取锁控板地址
 * info : 锁控板地址列表
 *
 */
int Java_com_ss_testserial_Startwrite_getBoardAddress(JNIEnv* env, jobject thiz, jintArray info) {
	int i,ret,boardIndex,j;
	int* buf;

	buf = (int*)(*env)->GetIntArrayElements(env,info,NULL);
	//协议五位  命令：0x81 板地址 0x01-0x0f  固定：0X01    状态：0X99    校检码：前面几位异或 0X19
	char xwdata[5] = { 0X81, 0X01, 0x01, 0x99 , 0x19};
	char xrdata[5] = { 0 };
	tcflush(uart_fd,   TCIOFLUSH);
	control_rs485('0');
	boardIndex = 0;
	//遍历核心板，询问是否有回应
	for(i=0; i<16; i++) {
		tcflush(uart_fd,   TCIOFLUSH);
		control_rs485('0');
		usleep(5000);
		xwdata[1] = i;
		xwdata[4] = (xwdata[0] ^ xwdata[1] ^ xwdata[2] ^ xwdata[3]) & 0xff;
		write(uart_fd, xwdata, 5);
		usleep(10000); //必须等待一段时间，rs485才会把数据发送出去
		control_rs485('1');
		ret = read_uart_data(xrdata,5);
				if(ret <= 0) {
					LOGD("BoardAdress: fail to get uart data\n");
				}
		LOGD("BoardAdress ret %d:%d 0x%x:0x%x:0x%x:0x%x:0x%x\n",
							ret, i,xrdata[0],xrdata[1],xrdata[2],xrdata[3],xrdata[4]);
		if(ret != 0 && (xrdata[0] == 0x81) &&
				((xrdata[0]^xrdata[1]^xrdata[2]^xrdata[3]^xrdata[4]) == 0x0)) {
			buf[boardIndex] = xrdata[1];
			boardIndex++;
		}
		usleep(200000);
	}
	LOGD("get address done\n");

	 (*env)->ReleaseIntArrayElements(env, info, (jint*)buf, JNI_ABORT);
	return boardIndex;
}

/*
 * 获取协议ID
 * cardID : 锁控板卡地址
 * info : 返回板卡程序协议
 */
int Java_com_ss_testserial_Startwrite_getProtocalID(JNIEnv* env, jobject thiz,jint cardID, jintArray info) {
	int i,ret,boardIndex,j;
	int* buf;
	buf = (int*)(*env)->GetIntArrayElements(env,info,NULL);
	//协议五位  命令：0X91, boardaddress, 0xfe, 0xfe , 0x6f
	char xwdata[5] = { 0X91, 0X0, 0xfe, 0xfe , 0x6f};
	char xrdata[5] = { 0 };

     xwdata[1] = cardID;
     xwdata[4] = (xwdata[0] ^ xwdata[1] ^ xwdata[2] ^ xwdata[3]) & 0xff;
	tcflush(uart_fd,   TCIOFLUSH);
	control_rs485('0');
	usleep(5000);
		write(uart_fd, xwdata, 5);
		usleep(10000); //必须等待一段时间，rs485才会把数据发送出去
		control_rs485('1');
		ret = read_uart_data(xrdata,5);
				if(ret <= 0) {
					LOGD("Protocal: fail to get uart data\n");
				}

		LOGD("Protocal ret %d, 0x%x:0x%x:0x%x:0x%x:0x%x\n",
				ret,xrdata[0],xrdata[1],xrdata[2],xrdata[3],xrdata[4]);

		for(i=0;i<5;i++)
				buf[i] = xrdata[i];
		 (*env)->ReleaseIntArrayElements(env,info, (jint*)buf, JNI_ABORT);
		return 0;


}
/**
 * 获取锁的状态
 *
 * cardID：板子标号 		doorID：门号				 info: 单片机返回的数据
 */
int Java_com_ss_testserial_Startwrite_getDoorState(JNIEnv* env, jobject thiz, jint boardID, jint doorID, jintArray info) {

	int i, len,ret;
	//协议五位  命令：0x80 板地址：0X01-0xC8   锁地址:0x00-0x18  命令:0x33    校检码：前面几位异或
	char xwdata[5] = { 0X80, (char)boardID, (char)doorID, 0x33};
	char xrdata[7] = { 0 };

	if(doorID == 0)
		len = 7;
	else
		len = 5;

	xwdata[4] = (xwdata[0] ^ xwdata[1] ^ xwdata[2] ^ xwdata[3]) & 0xff;
	//LOGD("get state send:0x%x:0x%x:0x%x:0x%x:0x%x\n",xwdata[0],xwdata[1],xwdata[2],xwdata[3],xwdata[4]);
	tcflush(uart_fd,   TCIOFLUSH);
	control_rs485('0');
	usleep(5000);
	write(uart_fd, xwdata, 5);
	usleep(10000);
	control_rs485('1');


	ret = read_uart_data(xrdata,len);
			if(ret <= 0) {
				LOGD("State: fail to get uart data\n");
			}
	//ret = read(uart_fd, xrdata, len);
	if((xrdata[0]==0x00) && (xrdata[1]==0x00)) {
						LOGD("###########get state fail. board %d door %d#################\n",boardID,doorID );
	}
	LOGD("state ret %d, 0x%x:0x%x:0x%x:0x%x:0x%x:0x%x:0x%x\n",
			ret,xrdata[0],xrdata[1],xrdata[2],xrdata[3],xrdata[4],xrdata[5],xrdata[6]);

	int* buf;

	buf = (int*)(*env)->GetIntArrayElements(env,info,NULL);

	for(i=0;i<len;i++)
			buf[i] = xrdata[i];
	 (*env)->ReleaseIntArrayElements(env,info, (jint*)buf, JNI_ABORT);
	return 0;
}

