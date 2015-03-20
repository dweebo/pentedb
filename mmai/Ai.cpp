#include <time.h>//for use of time() & symbolic constant NULL (also
#include <stdlib.h>//for use of rand() & srand() functions and
#include "Ai.h"
#include <iostream>
#include <fstream>
using namespace std;

#define HASH 1
CAi::CAi()
{
    int x, y,z;
    pAt = ATbl;
	ifstream file ("pente.tbl", ios::in|ios::binary|ios::ate);//[943][4] 
	if (file.is_open())
    {
	  file.seekg (0, ios::beg);
      for (x=0; x<3772; x++) 
	    file.read((char*)&ATbl[x], sizeof(short));

	  file.close();
    }
	else
	{
		printf("can't open\n");
	}

    pAs = AScr;	
	ifstream file2 ("pente.scs", ios::in|ios::binary|ios::ate);//[912][14]
	if (file2.is_open())
    {
	  file2.seekg (0, ios::beg);
      for (x=0; x<12768; x++)
        file2.read((char*)&AScr[x], sizeof(short));
	  
	  file2.close();
    }
	else
	{
		printf("can't open\n");
	}

	pFk=new int[1444];//361*4 //fukumi list
	//fukumi is a 4-3 threat:
	// .O.BO.
	// ...A..
	// ...O..
	// ...O..
	// ...X..
	 // if O moves to A, that threatens a win a B
	pFh=new int[3000];//750*4 //fukumi "holes"
	pTr=new int[363]; //temp total score list 
	pTm=new int[363]; //temp move list
	pTo=new int[2541]; //temp score list per each player
	pTi=new int[363]; // temp index
	erfl=0;
	a1=200; //for alpha/beta but not used currently
	b1=-200;

#if HASH == 1
	//hash table
	if ((pHashY = new unsigned int[1000000]) == NULL) erfl=1;
	if ((pHashD = new unsigned char[1000000]) == NULL) erfl=1;
	if ((pHashS = new short int[2000000]) == NULL) erfl=1;
	if (erfl) exit(1);
#endif

	srand( (unsigned)time( NULL ) );

	unsigned int seed=52356;
	for (int i=0; i<362; i++) { //generate pseudorandom table
		seed=(2416*seed+374441)%1771875; 
		TableX[i]=seed;
		seed=(2416*seed+374441)%1771875;
		TableY[i]=seed;
	}
	TableX[361]=2031; //SET lo
	TableY[361]=3201;
}

CAi::~CAi()
{
	delete[] pFk;
	delete[] pFh;
	delete[] pTr;
	delete[] pTm;
	delete[] pTo;
	delete[] pTi;

#if HASH == 1
	delete[] pHashY;
	delete[] pHashD;
	delete[] pHashS;
#endif

}

void CAi::Print(void)
{
	   int x,y,sc;
       for (y=0;y<19;y++) 
	   {
		  for (x=0;x<19;x++) 
		   {
		     if (bd[x][y]==-1) {
		       printf("C");
		     }
		     else printf("%d", bd[x][y]);
		   }
		  printf("\n");
	}
}

int CAi::Move(JNIEnv *env, jobject obj, jmethodID mid, jmethodID mid2,
              jsize numMoves, jint *movesp, jint level, jint vctin) // AI MAIN routine
{
    //printf("level=%d, vct=%d\n", level, vctin);
int i, x, y, x2,y2,icfl,cx, cy;
		
		int c1,c2,c3,c4,c5,c6,c7,c8,d;

int om2[8] = {181,182,162,163,164,165,144,145};
int op2[8] = {25,36,77,82,93,95,97,99};
int om3[32] = {183,184,202,221,240,260,239,238,237,256,236,235,
	234,252,215,196,177,176,158,139,120,100,
	121,122,123,104,124,125,126,108,145,164};

 //opening moves for turn 2 and 3.
//unsigned int mleft, avgt, tleft;

	dx[0]=dx[3]=dx[6]=-1;
	dy[0]=dy[1]=dy[2]=-1;
	dx[1]=dx[5]=dy[3]=dy[7]=0;
	dx[2]=dx[4]=dx[7]=1;
	dy[4]=dy[5]=dy[6]=1;
	rotx[0]=rotx[1]=rotx[2]=rotx[3]=1;
	rotx[4]=rotx[5]=rotx[6]=rotx[7]=-1;
	roty[0]=roty[1]=roty[6]=roty[7]=1;
	roty[2]=roty[3]=roty[4]=roty[5]=-1;
	rotf[0]=rotf[2]=rotf[4]=rotf[6]=0;
	rotf[1]=rotf[3]=rotf[5]=rotf[7]=1; 
	mxnd[1]=20; mxnd[2]=18; //number of nodes to expand at each level
	mxnd[3]=16; mxnd[4]=14;
	mxnd[5]=mxnd[6]=mxnd[7]=mxnd[8]=mxnd[9]=12;
	mxnd[10]=mxnd[11]=mxnd[12]=mxnd[13]=mxnd[14]=mxnd[15]=8;
	mxnd[16]=mxnd[17]=mxnd[18]=8;
	mxnd[0]=0;

	//pView=pdoc->pView;
	gf=0; 
	//Kgame=0; //1 = K-pente 
	multipbem=0; 
	np=2; 
 
    extnt = 2;

    for (x=0;x<19;x++)
      for (y=0;y<19;y++)
        bd[x][y]=0;

	cc[0][1]=0; //number of captures for p1 (at level 0)
	cc[0][2]=0; // "" p2
		
    //load up board with moves
	for (i = 0; i< numMoves; i++)
	{
		cp = i % 2 + 1;
		x = movesp[i] % 19;
		y = movesp[i] / 19;
		//printf("%d %d %d\n", cp, x, y);
        bd[x][y] = cp;

       for (x2=x-extnt; x2<x+1+extnt; x2++) //set spaces around piece to -1
		  for (y2=y-extnt; y2<y+1+extnt; y2++) //for consideration by ai
		    if (x2>=0 && x2<19 && y2>=0 && y2<19)
		      if (bd[x2][y2]==0) bd[x2][y2]=-1;

				  // chk captures
		  for (d=0; d<8; d++) {
			c1=x+dx[d];
			c2=y+dy[d];
			c3=c1+dx[d];
			c4=c2+dy[d];
			c5=c3+dx[d];
			c6=c4+dy[d];
			c7=c5+dx[d];
			c8=c6+dy[d];
			if (c5>=0 && c5<19 && c6>=0 && c6<19)
			  if (bd[c1][c2]>0 && bd[c3][c4]>0 &&
			      bd[c1][c2]!=cp && bd[c3][c4]!=cp) {
			    if (bd[c5][c6]==cp) {
			      cc[0][cp]+=2;
			      bd[c1][c2]=-1;
			      bd[c3][c4]=-1;
			    }
			    else {
			      if (c7>=0 && c7<19 && c8>=0 && c8<19 && Kgame)
				    if (bd[c7][c8]==cp && bd[c5][c6]>0) {
				      cc[0][cp]+=3;
				      bd[c1][c2]=-1;
				      bd[c3][c4]=-1;
				      bd[c5][c6]=-1;
				    }
			    }
			  } // if en*2
		  }  // next d
	}

    cp = 3-cp;
    turn = numMoves + 1;
	plv = level;

	gf = 0;

	/////////////input from somewhere ....//////////////////////////
	//cp=pDoc->cp; //current player
	//turn=pDoc->turn; //current turn
	//for (x=0; x<19; x++)
	//for (y=0; y<19; y++)
	//	bd[x][y]=pDoc->bd[x][y]; //BOARD
	//cc[0][1]=pDoc->cc[1]; //current captured pieces p1
	//cc[0][2]=pDoc->cc[2]; //current captured pieces p1
	//plv=pDoc->player[cp];  //depth of search 1-12
	//gf=pDoc->gf;  //set to 0
	///////////////////////////////////////////////////////////////

//Print();
//printf("cp = %d\n", cp);

	vct=vctin; //threat search


    //printf("level=%d, vct=%d\n", plv, vct);
	tourn=1; //tournament rule
	breadth=1;
	extent=0;
	//int mxvt[13]={0,1,3,4,6,7,8,9,10,12,13,14,15}; 
	//int mxvf[13]={0,1,4,5,7,8,10,11,13,15,16,17,18}; 
	mxvt[0]=mxvf[0]=0; //limits for vct
	mxvt[1]=mxvf[1]=1;
	mxvt[2]=3;
	mxvt[3]=mxvf[2]=4;
	mxvf[3]=5;
	mxvt[4]=6;
	mxvt[5]=mxvf[4]=7;
	mxvt[6]=mxvf[5]=8;
	mxvt[7]=9;
	mxvt[8]=mxvf[6]=10;
	mxvf[7]=11;
	mxvt[9]=12;
	mxvt[10]=mxvf[8]=13;
	mxvt[11]=14;
	mxvt[12]=mxvf[9]=15;
	mxvf[10]=16;
	mxvf[11]=17;
	mxvf[12]=18;
	for (i=13; i<19; i++) { mxvf[i]=18; mxvt[i]=18; }

	ciel[0][1]=ciel[0][2]=24;

	bmove=0;
	bscr=0;
	exfl[0]=3;
	exel[0]=3;
	mxst=2;
	srand((unsigned)time(NULL));

	if (gf==3) //just score pt
	{
	  gf=0;
	  lvl=0;
	  for (i=0; i<1444; i++)    //clear fukumi/legal table
		*(pFk+i)=0;
	  fr=cp;
	  fhn=0;
	  //pDoc->bscr=Score(pDoc->pxy[0]);
	  //pDoc->cap1=cap1;           //return vals
	  //for (x=0; x<cap1; x++)
		//pDoc->pxy[x]=p1xy[x];
	  return 0;
	}
	if (turn==1) bmove=180;
	if (turn==2) {
		do {
			x=7+rand()%5;
			y=7+rand()%5;
		} while (bd[x][y]>0);
		bmove=y*19+x;
	}
	if (turn==3) {
		do {
			i=rand()%32;
			x=om3[i]%19;
			y=om3[i]/19;
			icfl=bd[x][y];
		} while (icfl>0);
		bmove=y*19+x;
	}

	maxscr=32000;

	if (!bmove) {
		alpha=32000;
		beta=-32000;
		plv=Tree(env, obj, mid, mid2);
		x=1;
	} 

	//pDoc->bscr=bscr; //output best score
	//pDoc->bmove=bmove; //output best move (y*19 + x)
//Print();
//printf("move=%d,score=%d\n",bmove,bscr);
return 0;
}

int CAi::Tree(JNIEnv *env, jobject obj, jmethodID mid, jmethodID mid2)
{
int minscr, ctfl, mxlv, mxor[19]; 
int mv[19], mvsco[19][363][7], mvscr[19][363], mvlst[19][363];
int scr[19][7], hmv[19], mxmv[19], exstkx[19][36], exstky[19][36];
int nstk[19], ncap[19], capx[19][24], capy[19][24], capv[19][24];
// scr[0] is the computers final score after the search
// hmv is the best move found in the format x+y*19
int *pmvscr, *pmv, *pmxmv, *pmvlst, *pmxor, *pexfl, *pexel;
int fl1, wfl, ii, x, y, i, j, frmo, rxy, rx, ry, hfl;
int xx, yy, d, c1, c2, c3, c4, c5, c6, c7, c8, ct, sc;
int shi, shj, shv, shw, inc, loc, tyt, tyf, tys; 
int htempx, cpx, cpy, cutfl;

unsigned int htempy;

	hmv[0]=0;
	mvlst[1][0]=-1; 
	wfl=0;
	lvl=0;
	cutfl=1;
	sec[0]=0;
	//pDoc->Esc = FALSE;
	fl1=1;
	extnt=extent+2;
	for (i=1; i<19; i++) {
		mxor[i]=mxnd[i];
		if (breadth==2) 
		mxor[i]=mxor[i]*2;
	}

#if HASH == 1
	HValX[0]=HValY[0]=0; //init hash pos
	for ( x=0; x<19; x++ ) 
	for ( y=0; y<19; y++)
	if (bd[x][y]==1 || bd[x][y]==2) {
		HValX[0] ^= (bd[x][y]*TableX[x+19*y]);
		HValY[0] ^= (bd[x][y]*TableY[x+19*y]);
	}
	for (x=0; x<1000000; x++) *(pHashD+x)=0;
#endif

do { //A
	do { //B
	lvl++;
	pmvscr=mvscr[lvl];
	pmv=&mv[lvl];
	pmxmv=&mxmv[lvl];
	pmvlst=mvlst[lvl];
	pmxor=&mxor[lvl];

	#if HASH == 1
		HValX[lvl]=HValX[lvl-1];
		HValY[lvl]=HValY[lvl-1];
	#endif

	for (x=1; x<=np; x++)
		cc[lvl][x]=cc[lvl-1][x];
	fr=cp-1+lvl;
	while (fr>np) fr-=np;
	en=fr+1;
	if (en>np) en=1;
	if (fl1) {
	for (i=1; i<=np; i++) {
		ciel[lvl][i]=ciel[lvl-1][i];
		scr[lvl-1][i]= beta;
	}
	for (i=0; i<1444; i++) //clear fukumi table
	*(pFk+i)=0;
	fhn=0;
	ferr=0;
	*pmv=-1;
	mvct=1;

	for (i=0; i<*pmxor; i++) *(pmvscr+i)=-30000;
	minscr=-30000;

	for (x=0; x<19; x++)
	for (y=0; y<19; y++)
	if (bd[x][y]==-1) {
		sc = Eval(env, obj, mid, mid2, x,y);
		if (np==2)
		if (-sco[3-fr]>800 && minscr<3000)
		minscr=3000; //block four
		*(pTr+mvct)=sc;
		*(pTm+mvct)=y*19+x;
		for (ii=1; ii<=np; ii++)
		*(pTo+mvct*7+ii)=sco[ii];
		*(pTi+mvct)=mvct;
		mvct++;
	} // end if spc
	for (j=0; j<fhn; j++) { //add in fukumi
		loc=*(pFh+j*4+1);
		tyt=tyf=0;
		for (i=0; i<4; i++)
		if (i!=*(pFh+j*4+3)) {
			tys=*(pFk+loc*4+i);
			if (tys==6) tyt++;
			if (tys==5) tyt++;
			if (tys==7 || tys==8) tyf++;
		}
		tys=*(pFh+j*4+2);
		if (tys==2 && tyf) *(pTr+*(pFh+j*4))+=100;
		if (tys==3 && tyt) *(pTr+*(pFh+j*4))+=100;
		if (tys==3 && tyf) *(pTr+*(pFh+j*4))+=100;
		if (tys==2 && tyt) *(pTr+*(pFh+j*4))+=50;
	}

	mvct--;
	
	
	//for (i=0; i<mvct-1; i++)  printf("%d=%d\n",*(pTr+i+1),*(pTm+*(pTi+i+1)));
	
	inc=1; //shell sort
	do { inc*=3; inc++; }
	while (inc<=mvct);
	do {
		inc/=3;
		for (shi=inc+1; shi<=mvct; shi++) {
			shv=*(pTr+shi);
			shw=*(pTi+shi);
			shj=shi;
			while (*(pTr+shj-inc)<shv) {
				*(pTr+shj)=*(pTr+shj-inc);
				*(pTi+shj)=*(pTi+shj-inc);
				shj-=inc;
				if (shj<=inc) break;
			}
			*(pTr+shj)=shv;
			*(pTi+shj)=shw;
		}
	} while (inc>1);
	ct=0;
	for (i=0; i<mvct-1; i++) { //keep best ~26
			//printf("%d=%d\n",*(pTr+i+1),*(pTm+*(pTi+i+1)));
		if (*(pTr+i+1)>minscr && ct<*pmxor) {
			ct++;
			j=*(pTi+i+1);
			*(pmvscr+i) = *(pTr+i+1);
			*(pmvlst+i) = *(pTm+j);
			//printf("%d=%d\n",*(pmvlst+i),*(pmvscr+i));
			for (ii=1; ii<=np; ii++)
			mvsco[lvl][i][ii] = *(pTo+j*7+ii);
		}
		else break;
	}
	if (ct<1 && mvct>0) {
		ct=1;
		j=*(pTi+1);
		*(pmvscr) = *(pTr+1);
		*(pmvlst) = *(pTm+j);
		for (ii=1; ii<=np; ii++)
		mvsco[lvl][0][ii] = *(pTo+j*7+ii);
		//errmsg(3);
	}
	if (ct<*pmxor) *pmxmv=ct;
	else *pmxmv=*pmxor;
	} // end fl1

	(*pmv)++; //next move
	x=(*(pmvlst+(*pmv)))%19;
	y=(*(pmvlst+(*pmv)))/19;
	wfl=0;
	sc=*(pmvscr + (*pmv));
	sco[fr]=mvsco[lvl][*pmv][fr]; //used in VCT

	if (vct) { //VCT
		pexfl=&exfl[lvl];
		pexel=&exel[lvl];
		*pexfl=exfl[lvl-1];
		*pexel=exel[lvl-1];
		ctfl=1;
		if (*pmxmv<2) ctfl=0;
		if (fr==cp && ctfl) {
			if (sco[fr]<520 && *pexfl>1 && lvl>=mxst && lvl>mxvt[plv])
				*pexfl=*pexfl-2;
			if (sco[fr]<110 && *pexfl>1 && lvl>=mxst)
				*pexfl=*pexfl-2;
			if (sco[fr]<110 && (*pexfl)%2 && sc<1800) (*pexfl)--;
		}
		if (fr!=cp && ctfl) {
			if (sco[fr]<520 && *pexel>1 && lvl>=mxst && lvl>mxvt[plv])
				*pexel=*pexel-2;
			if (sco[fr]<110 && *pexel>1 && lvl>=mxst)
				*pexel=*pexel-2;
			if (sco[fr]<110 && (*pexel)%2 && sc<1800) (*pexel)--;
		}
		if (*pexfl<2 && *pexel<2 && lvl>=mxvt[plv]) mxlv=lvl;
		if (!(*pexfl) && !(*pexel) && lvl>=plv) mxlv=lvl;
		if (lvl<=plv) mxlv=plv;
		if ((*pexfl>1 || *pexel>1) && lvl==mxlv && lvl<mxvf[plv]) mxlv++;
		if (((*pexfl)%2 || (*pexel)%2) && lvl==mxlv && lvl<mxvt[plv]) mxlv++;
	} // end vct
	else mxlv=plv;
	if (mxlv>ciel[lvl][cp]) 
	mxlv=ciel[lvl][cp];

	hfl=0;
	if (lvl<mxlv && *pmv<*pmxmv) {

	bd[x][y]=fr; // make move
	nstk[lvl]=0;
	for (xx=x-extnt; xx<x+1+extnt; xx++)
	for (yy=y-extnt; yy<y+1+extnt; yy++)
	if (xx>=0 && xx<19 && yy>=0 && yy<19)
	if (!bd[xx][yy]) {
		bd[xx][yy]=-1;
		exstkx[lvl][nstk[lvl]]=xx;
		exstky[lvl][nstk[lvl]++]=yy;
	}

#if HASH == 1
	HValX[lvl] ^= (bd[x][y]*TableX[x+y*19]);
	HValY[lvl] ^= (bd[x][y]*TableY[x+y*19]);
#endif

	 // chk capture
	ncap[lvl]=0;
	for (d=0; d<8; d++) { 
		c1=x+dx[d];
		c2=y+dy[d];
		c3=c1+dx[d];
		c4=c2+dy[d];
		c5=c3+dx[d];
		c6=c4+dy[d];
		c7=c5+dx[d];
		c8=c6+dy[d];
		if (c5>=0 && c5<19 && c6>=0 && c6<19)
		if (bd[c1][c2]>0 && bd[c3][c4]>0 &&
			bd[c1][c2]!=fr && bd[c3][c4]!=fr) {
			if (bd[c5][c6]==fr) {
				cc[lvl][fr]+=2;
				capx[lvl][ncap[lvl]]=c1;
				capy[lvl][ncap[lvl]]=c2;
				capv[lvl][ncap[lvl]++]=bd[c1][c2];
				capx[lvl][ncap[lvl]]=c3;
				capy[lvl][ncap[lvl]]=c4;
				capv[lvl][ncap[lvl]++]=bd[c3][c4];
				bd[c1][c2]=-1;
				bd[c3][c4]=-1;

#if HASH == 1
				HValX[lvl] ^= (en*TableX[c1+19*c2]);
				HValY[lvl] ^= (en*TableY[c1+19*c2]);
				HValX[lvl] ^= (en*TableX[c3+19*c4]);
				HValY[lvl] ^= (en*TableY[c3+19*c4]);
#endif

			}
			else {
				if (c7>=0 && c7<19 && c8>=0 && c8<19 && Kgame)
				if (bd[c7][c8]==fr && bd[c5][c6]>0) {
					cc[lvl][fr]+=3;
					capx[lvl][ncap[lvl]]=c1;
					capy[lvl][ncap[lvl]]=c2;
					capv[lvl][ncap[lvl]++]=bd[c1][c2];
					capx[lvl][ncap[lvl]]=c3;
					capy[lvl][ncap[lvl]]=c4;
					capv[lvl][ncap[lvl]++]=bd[c3][c4];
					capx[lvl][ncap[lvl]]=c5;
					capy[lvl][ncap[lvl]]=c6; 
					capv[lvl][ncap[lvl]++]=bd[c5][c6];
					bd[c1][c2]=-1;
					bd[c3][c4]=-1;
					bd[c5][c6]=-1;

#if HASH == 1
					HValX[lvl] ^= (en*TableX[c1+19*c2]);
					HValY[lvl] ^= (en*TableY[c1+19*c2]);
					HValX[lvl] ^= (en*TableX[c3+19*c4]);
					HValY[lvl] ^= (en*TableY[c3+19*c4]);
					HValX[lvl] ^= (en*TableX[c5+19*c6]);
					HValY[lvl] ^= (en*TableY[c5+19*c6]);
#endif
				}
			}
		} // if en*2
	} // next d

	#if HASH == 1	
		htempx=(HValX[lvl] ^(TableX[361]*(cc[lvl][1]+cc[lvl][2])))%1000000;
		htempy= HValY[lvl] ^(TableY[361]*(cc[lvl][1]+cc[lvl][2]));
		if (*(pHashY+htempx)==htempy && np<3)
		if (*(pHashD+htempx)==lvl) { //found in table!
			sc=*(pHashS+htempx*2+fr-1);
			scr[lvl][fr]=sc;
			scr[lvl][3-fr]=-sc; 
			if (sc>10000 || sc<-10000) wfl=1;
			hfl=1;
		}
	#endif

	if (sc>=10000 && !hfl) { //check win

		scr[lvl][fr]=12000-lvl;
		scr[lvl][3-fr]=lvl-12000;
		wfl=1;
		if (lvl==1) {
			scr[lvl][fr]=30000;
			hmv[0]=mvlst[1][mv[1]];
			wfl++;
		}
	} // end win
	if (lvl==1 && *pmxmv==1) wfl=1; //forced move
	} // end non-maxlv
	fl1=1;
	} while (*pmv<*pmxmv && lvl<mxlv && !wfl && !hfl);
	fl1=0;
	if (*pmv>=*pmxmv) { //no more moves
		lvl--;
		fr--;
		if (!fr) fr=np;
	}
	else if (lvl==mxlv) {
	hmv[lvl]=mvlst[1][mv[1]];
	if (sc>10000) { // win
			scr[lvl][fr]=12000-lvl;
			scr[lvl][3-fr]=lvl-12000;
	} //end win
	else {
		for (i=1; i<=np; i++) sco[i]=0; //add up scores
		for (ii=1; ii<=lvl; ii++)
		for (i=1; i<=np; i++) {
			sco[i]+=mvsco[ii][mv[ii]][i];
			if (sco[i]>7800) sco[i]=7800;
		}
		en=3-fr;
		scr[lvl][fr]=sco[fr]-sco[en]*4+rand()%6;
		scr[lvl][en]=-scr[lvl][fr];
		for (ii=1; ii<=np; ii++) {
			if (scr[lvl][ii]>10000) scr[lvl][ii]=10000;
			if (scr[lvl][ii]<-10000) scr[lvl][ii]=-10000;
		}
	} // end else sco[fr]>10000
	} // end if maxlv

	if (wfl) {
		hmv[lvl]=mvlst[1][mv[1]];
	//mv[lvl]=mxmv[lvl];
	}

	if (lvl>0) {
		if (scr[lvl][fr] > scr[lvl-1][fr] 
		&& (lvl>1 || scr[lvl][fr]<maxscr)) {
			if (12000-scr[lvl][fr]<ciel[lvl][cp])
				ciel[lvl][cp]=12000-scr[lvl][fr];
			for (i=1; i<=np; i++)
			scr[lvl-1][i]=scr[lvl][i];
			hmv[lvl-1]=hmv[lvl];
#if HASH == 1
			if (lvl<mxlv) {
				htempx=(HValX[lvl] ^ (TableX[361]*
				(cc[lvl][1]+cc[lvl][2])))%1000000;
				htempy= HValY[lvl] ^(TableY[361]*(cc[lvl][1]+cc[lvl][2]));
				*(pHashY+htempx)=htempy;

				if (scr[lvl][fr]==beta) //|| sc==alpha
					*(pHashD+htempx)=128+lvl;
				else *(pHashD+htempx)=lvl;
				for (i=1; i<3; i++) 
					*(pHashS+htempx*2+i-1)=(short) scr[lvl][i];
			}
#endif

		}
		frmo=fr-1;
		if (frmo<1) frmo+=np;
		if (lvl > 1)
		if (scr[lvl][frmo] <= scr[lvl-2][frmo]) {
			if (lvl<mxlv) {
				rxy=mvlst[lvl][mv[lvl]];
				ry=rxy/19;
				rx=rxy%19;
				bd[rx][ry]=-1;
				for (ii=0; ii<ncap[lvl]; ii++) {
					cpx=capx[lvl][ii];
					cpy=capy[lvl][ii];
					bd[cpx][cpy]=capv[lvl][ii];
				}
				for (ii=0; ii<nstk[lvl]; ii++)
				bd[exstkx[lvl][ii]][exstky[lvl][ii]]=0;
			}
			lvl--;
		}
		if (np==2 && lvl==1 && scr[0][cp]<12000 && 12000-scr[0][cp]<ciel[0][cp])
		ciel[0][cp]=12000-scr[0][cp];
	} //lvl >0

	if (lvl>0 && lvl<mxlv) {
		rxy=mvlst[lvl][mv[lvl]];
		ry=rxy/19;
		rx=rxy%19;
		bd[rx][ry]=-1;
		for (ii=0; ii<ncap[lvl]; ii++) {
			cpx=capx[lvl][ii];
			cpy=capy[lvl][ii];
			bd[cpx][cpy]=capv[lvl][ii];
		}
		for (ii=0; ii<nstk[lvl]; ii++)
		bd[exstkx[lvl][ii]][exstky[lvl][ii]]=0;
	}
	lvl--;

    if (stopped) {
		printf("tree will stop now\n");
		wfl=2;
		ii=cp+1;
		if (ii>np) ii=1;
		stopped = 0;
    } 
    //else printf("not stopped\n");

	/*
	 if (pDoc->Esc) {
	 //check for ESC key
	 pDoc->Esc=FALSE;
		wfl=2;
		ii=cp+1;
		if (ii>np) ii=1;
	 pDoc->player[ii]=0;
	 } */

	 //time (&t2); //update time
	 //sec[0]=t2-ti;

} while (lvl>=0 && wfl<2);

	if (wfl==2 && lvl>0) {
		if (lvl==mxlv) lvl--;
		for (i=lvl; i>0; i--) {
			rxy=mvlst[i][mv[i]];
			ry=rxy/19;
			rx=rxy%19;
			bd[rx][ry]=-1;
			for (ii=0; ii<ncap[i]; ii++) {
				cpx=capx[i][ii];
				cpy=capy[i][ii];
				bd[cpx][cpy]=capv[i][ii];
			}
			for (ii=0; ii<nstk[i]; ii++)
				bd[exstkx[i][ii]][exstky[i][ii]]=0;
		}
	}
 
	bmove=hmv[0];

	if (!bmove) bmove=mvlst[1][0];
	bscr=scr[0][cp];
	if (mxmv[1]==1) bscr=0;

return 0;
} 

int CAi::Eval(JNIEnv *env, jobject obj, jmethodID mid, jmethodID mid2,
              int x, int y)
{

CPoint pt;
int s0, i, j,s[7], tcap2, tcap3;
int x9, y9, bl, tfr, tcap1;
int k=0;

(*env).CallVoidMethod(obj, mid);

/* vis callbacks can be toggled on/off */
if (callbacks)
{
  /* sort of wasteful to copy bd twice but worry about it later */
  jintArray c;
  c = (*env).NewIntArray(361);
  if (c == NULL) {
     printf("out of memory\n");
  }
  jint temp[361];
  for (i=0;i<19;i++) for (j=0;j<19;j++) {
      temp[k++] = bd[i][j];
  }
  temp[x*19+y] = 3;
  (*env).SetIntArrayRegion(c, 0, 361, temp);
  (*env).CallVoidMethod(obj, mid2, c);
  (*env).DeleteLocalRef(c);
}
  
lvl--;
for (i=1; i<7; i++) s[i]=0;
gf=0;
pt.x=x;
pt.y=y;
s0=Score(pt); //s0=0

if (s0>10000) {
	for (i=1; i<=np; i++) sco[i]=-12000;
		sco[fr]=12000;
}
else {
	for (i=1; i<=np; i++) s[i]=sco[i];
	gf=1;
	x9=x; y9=y;
	tfr=fr;
	tcap1=cap1;
	for (bl=0; bl<tcap1; bl++ ) { //score captured
		x=p1xy[bl].x; y=p1xy[bl].y;
		ppd=p1d[bl];
		if (ppd>4) ppd=ppd-4;
		fr=3-tfr;
		pt.x=x;
		pt.y=y;
		s0=Score(pt);
		sco[1]=sco[1]-sco[1]/10;
		sco[2]=sco[2]-sco[2]/10;
		s[1]-=sco[1];
		s[2]-=sco[2];

	} // next bl
	if (np==2) tcap2=cap2;
	else tcap2=0;
	for (bl=0; bl<tcap2; bl++ ) { //threatened
		x=p2xy[bl].x; y=p2xy[bl].y;
		ppd=p2d[bl];
		if (ppd>4) ppd=ppd-4;
		fr=3-tfr;
		pt.x=x;
		pt.y=y;
		s0=Score(pt);
		s[1]-=sco[1]/8;
		s[2]-=sco[2]/8;
	} // next bl
	tcap3=cap3;
	for (bl=0; bl<tcap3; bl++ ) { //protected
		x=p3xy[bl].x; y=p3xy[bl].y;
		ppd=p3d[bl];
		if (ppd>4) ppd=ppd-4;
		fr=tfr;
		pt.x=x;
		pt.y=y;
		s0=Score(pt);
		s[1]+=sco[1]/8;
		s[2]+=sco[2]/8;
	} // next bl
	fr=tfr;
	x=x9; y=y9;
 
	for (i=1; i<=np; i++) {
		sco[i]=s[i];
		if (sco[i]>7800) sco[i]=7800;
	}
	s0=sco[fr]-sco[3-fr]*4;

	if (s0>9500) s0=9500;
} //end else

gf=0;

lvl++;
	//printf("Eval(%d,%d)=%d\n",x,y,s0);
return s0;
}

int CAi::Score(CPoint pt)
{
int dv, cx, cy, iw, qs, po, tys, hlim, g1, s0;
int i, x, y, f0, side, sign, index, c4, c5;
int f1[2], sp[2], la[5], lb[9], c2[7], c3[7], g[2], lc[5], ld[9];
int item, fl, lx, ly, df, iv, lf[3];

//g values for O
//0 ?.
//1 ?X_
//2 ?XX_
//3 ?XXO
//4 ?XXX_
//5 ?XXXO
//6 ?O_
//7 ?OO_
//8 ?_
//9 ?OOX ##############

x=pt.x;
y=pt.y;
cap1=cap2=cap3=0;
c4=c5=0;
dv=0;
for (i=1; i<7; i++) sco[i]=c3[i]=0;
hlim=4;
if (Kgame) hlim=5;

do { //c0
if (gf==1 && dv==ppd) { //just need c4 - eval captured stone
	for (sign=-1; sign<2; sign+=2) { //look for captures
		for (iw=1; iw<hlim; iw++) {
			lc[iw]=0;
			cx=x+dx[dv]*iw*sign;
			cy=y+dy[dv]*iw*sign;
			if (cx>=0 && cx<19 && cy>=0 && cy<19) {
				qs=bd[cx][cy];
				if (qs>0) la[iw]=qs;
				else la[iw]=0;
			} else la[iw]=-1;
		} //iw
		if (la[1]>0 && la[1]!=fr)
		if (la[2]>0 && la[2]!=fr) { //b0
			if (la[3]==fr) c4+=2; //pair now open for capture
			if (!la[3]) c5+=2; //pair no longer open for capture
			if (la[3]>0 && la[3]!=fr && Kgame) {
				if (la[4]==fr) c4+=3;
				if (!la[4]) c5+=3;
			}
		} // b0
	} //sign
	dv++;
} //gf/dv

if (dv<4) {
	for (sign=-1; sign<2; sign+=2) { //look for captures
		lf[sign+1]=-1;
		for (iw=1; iw<hlim; iw++) {
			lc[iw]=0;
			cx=x+dx[dv]*iw*sign; //first
			cy=y+dy[dv]*iw*sign;
			if (cx>=0 && cx<19 && cy>=0 && cy<19) {
				qs=bd[cx][cy];
				if (qs>0) {
					la[iw]=qs;
					if (lf[sign+1]<0) lf[sign+1]=qs;
				} else la[iw]=0;
			} else la[iw]=-1;
		} //iw
		g1=0;
		if (!la[1]) g1=8;
		if (la[1]==fr) {
			if (!la[2]) g1=6;
			if (la[2]==fr && !la[3]) g1=7;
			if (la[2]==fr && np==2 && !gf) { //protected pair
				if (la[3]>0 && la[3]!=fr && !Kgame) { //g1=9
					for (i=1; i<3; i++) { 
						p3xy[cap3].x=x+dx[dv]*sign*i;
						p3xy[cap3].y=y+dy[dv]*sign*i;
						p3d[cap3]=dv;
						cap3++;
					}
				}
			}//prot pr 
		}
		if (la[1]>0 && la[1]!=fr) { //en
			if (!la[2]) g1=1;
			if (la[2]>0 && la[2]!=fr) { //b0
				if (!la[3]) { //threaten
					g1=2;
					if (!gf) for (i=1; i<3; i++) { //threatened pair
						p2xy[cap2].x=x+dx[dv]*sign*i;
						p2xy[cap2].y=y+dy[dv]*sign*i;
						p2d[cap2]=dv;
						cap2++;
					}
				}

				if (la[3]==fr && !gf) {
					g1=3;
					for (i=1; i<3; i++) {
						p1xy[cap1].x=x+dx[dv]*sign*i;
						p1xy[cap1].y=y+dy[dv]*sign*i;
						p1d[cap1]=dv;
						cap1++;
						lc[i]=la[i];
						la[i]=0;
					}
				}
				if (la[3]==fr && gf==1) {
					g1=0;
					c4+=2;
				}
				if (la[3]>0 && la[3]!=fr && Kgame) {
					if (la[4]==fr && !gf) {
						g1=5;
						for (i=1; i<4; i++) {
							p1xy[cap1].x=x+dx[dv]*i*sign;
							p1xy[cap1].y=y+dy[dv]*i*sign;
							p1d[cap1]=dv;
							cap1++;
							lc[i]=la[i];
							la[i]=0;
						}
					}
					if (la[4]==fr && gf==1) {
						g1=0;
						c4+=3;
					}
					if (!la[4]) g1=4;
				}
			} // b0
		} // en
		iw=0; if (sign>0) iw=1;
		for (i=1; i<hlim; i++) {
			lb[i+iw*4]=la[i];
			ld[i+iw*4]=lc[i];
		}
		g[iw]=g1;

	} //sign
	for (i=1; i<=np; i++) c2[i]=0;
	sp[0]=sp[1]=0;
	if (g[0]==8 || g[0]==3 || g[0]==5) sp[0]=1;
	if (g[1]==8 || g[1]==3 || g[1]==5) sp[1]=1;
	for (i=0; i<2; i++) {
		if (g[i]==6 && sp[1-i]) { // pairs
			if (Kgame) sco[fr]-=20;
			else sco[fr]-=12;
		}
		if (Kgame && g[i]==7 && sp[1-i]) sco[fr]-=12;
		if (!Kgame && g[i]==7) sco[fr]+=12;
		if (g[i]==2) { // threaten a pair
			sco[fr]+=50;
			if (cc[lvl][fr]+cap1>Kgame*5+7) sco[fr]+=1024;
		}
		if (Kgame && g[i]==4) {
			sco[fr]+=75;
			if (cc[lvl][fr]+cap1>Kgame*5+6) sco[fr]+=1024; 
		}
	} // next i
	if (Kgame && g[0]==6 && g[1]==6) sco[fr]-=12; //pair
	for (iw=0; iw<5; iw+=4) { // O = played
		for (i=1; i<=np; i++) { // Z = potential capturer
			if (i!=fr && lb[iw+1]>0 && lb[iw+2]>0) // OXYZ
			if (lb[iw+1]!=i && lb[iw+2]!=i) { // protect
				if (lb[iw+3]==i && (!Kgame || lb[5-iw])) c3[i]+=2;
				if (Kgame && lb[iw+3]!=i && lb[iw+3]>0 && lb[iw+4]==i) c3[i]+=3;
			}
		} // next i
		i=lb[5-iw];
		if (i>0 && i!=fr)
		if (lb[iw+1]>0 && lb[iw+1]!=i) { // _XOZ
			if (!lb[iw+2]) c2[i]+=2; // make suscept. pair
			if (Kgame && lb[iw+2]>0 && lb[iw+2]!=i && !lb[iw+3]) c2[i]+=3;
		}
		i=lb[6-iw];
		if (i>0 && i!=fr)
		if (lb[5-iw]>0 && lb[5-iw]!=i) { // _OXZ
			if (!lb[iw+1]) c2[i]+=2;
			if (Kgame && lb[iw+1]>0 && lb[iw+1]!=i && !lb[iw+2]) c2[i]+=3;
		}
		i=lb[7-iw];
		if (i>0 && i!=fr && Kgame) // _OXYZ
		if (lb[6-iw]>0 && lb[6-iw]!=i && lb[5-iw]>0 && lb[5-iw]!=i)
		if (!lb[iw+1]) c2[i]+=3;
	} // next iw
	for (i=1; i<=np; i++) {
		if (i!=fr) { // c2 is stones now able to take
			s0=c2[i]*25;
			if (cc[lvl][i]+c2[i]>Kgame*5+9) s0=s0+2048;
			sco[i]+=s0;
		}
	} // next i
	// end capt

	po=0;
	f0=-1;

	do { //c1 //look up in table
		f1[0]=f1[1]=-1;
		index=0;
		fl=sign=1;

		if (lf[0]!=fr && lf[2]!=fr) //eval pre-capt
		for (i=1; i<hlim+3; i++)
		if (!lb[i] && ld[i] && i!=4) lb[i]=ld[i];
		do { //c2
			iw=*(pAt+index*4+0)*sign;
			side=1; iv=iw+4;
			if (iw<0) { side=0; iv=-iw; }
			qs=-2;
			if (iw>-hlim && iw<hlim) qs=lb[iv];
			if (qs<-1) {
				cx=x+dx[dv]*iw;
				cy=y+dy[dv]*iw;
				if (cx>=0 && cx<19 && cy>=0 && cy<19) {
					qs=bd[cx][cy];
					if (qs<0) qs=0;
				}
				else qs=-1;
			}
			if (qs>-1) {
				if (qs>0) {
					if (f0==-1) f0=qs;
					if (f1[side]==-1) f1[side]=qs;
					if (qs!=f0) {
						item=1;
						if (qs==f1[side]) po++;
					}
					else item=3;
				}
				else item=2;
			}
			else item=1;
			index=*(pAt+index*4+item);
			if (index<0) {
				index=-index;
				sign=-sign;
			}
			if (index>9999) fl=0;
		} while (fl); //c2
		index-=10000;
		tys=*(pAs+index*14+5); //white pattern
		if (f0==fr) *(pFk+(x+y*19)*4 +dv)=tys;

		//score for friend / enemy
		if (f0==fr) {
			if (np<3 || multipbem) df=*(pAs+index*14+2);
			else df=*(pAs+index*14+0);
			sco[fr]+=df;

			if (!gf) {

				if (tys==2 || tys==3)
				for (i=0; i<*(pAs+index*14+7); i++) {
					pfhn=pFh+fhn*4;	 
					*(pfhn+2)=tys;
					lx=x+dx[dv]*(*(pAs+index*14+8+i))*sign;
					ly=y+dy[dv]*(*(pAs+index*14+8+i))*sign;
					*(pfhn+1)=lx+ly*19;
					*(pfhn+0)=mvct;
					*(pfhn+3)=dv;
					fhn++;
					if (fhn>749) {
						fhn=749;//errmsg(0);
						ferr++;
					}
				}
				if (tys==4 || tys==11 || tys==12)
				for (i=0; i<*(pAs+index*14+7); i++) {
					pfhn=pFh+(fhn)*4;
					if (tys==4 && i<2 || tys==11 && i>1 || tys==12 && i<2)
					*(pfhn+2)=3;
					else *(pfhn+2)=2;
					lx=x+dx[dv]*(*(pAs+index*14+8+i))*sign;
					ly=y+dy[dv]*(*(pAs+index*14+8+i))*sign;
					*(pfhn+1)=lx+ly*19;
					*(pfhn+0)=mvct;
					*(pfhn+3)=dv;
					fhn++;
					if (fhn>749) {
						fhn=749; //errmsg(0);
						ferr++;
					}
				}
			} //np=2, !gf
		} //friend
		else {
			sco[f0]-=*(pAs+index*14+3);
		}
		if (f0==f1[0]) f0=f1[1]; //eval other player
		else f0=f1[0];

	} while (po==1); //c1

	dv++;
} //!dv==4

} while (dv<4 && sco[fr]<10000); // c0


for (i=1; i<=np; i++) {
	if (i!=fr) { 
		s0=-c3[i]*25; // c3 is stones now blocked from capture
		if (cc[lvl][i]+c3[i]>Kgame*5+9) s0=s0-1024;
		sco[i]+=s0;
	}
} // next i
sco[fr]+=cap1*160; // captures
s0=(c4-c5)*25; // c4 is stones now open for capture
if (cc[lvl][fr]+c4>Kgame*5+9) s0=s0+1024;
if (cc[lvl][fr]+c5>Kgame*5+9) s0=s0-1024;
sco[fr]-=s0; //is subtracted in eval
if (cc[lvl][fr]+cap1>Kgame*5+9) sco[fr]=12000;

if (sco[fr]>12000) sco[fr]=12000;
s0=sco[fr];

	//printf("Score(%d,%d)=%d,%d\n",pt.x,pt.y,sco[0],sco[1]);
	
return s0;
}
