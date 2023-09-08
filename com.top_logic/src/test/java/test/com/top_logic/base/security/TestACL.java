
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.security;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.security.authorisation.roles.ACL;

/**
 * Test class for the ACL.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestACL extends BasicTestCase {

    private String[] roles = new String[2000];

    /**
     * Constructor needed for framework.
     *
     * @param    aName    The name of the test.
     */
    public TestACL (String aName) {
        super (aName);
    }

    /**
     * Test the Copy Contructor
     */
    public void testCopyCTor () {
        ACL theOrig = new ACL ("kratz,mich,doch");
        ACL theCopy = new ACL(theOrig);
        
        assertEquals(theOrig.getACLString(), theCopy.getACLString());
        assertEquals(theOrig, theCopy);

        theOrig = new ACL ();
        theCopy = new ACL(theOrig);

        assertEquals(theOrig.getACLString(), theCopy.getACLString());
        assertEquals(theOrig, theCopy);

        theOrig = new ACL ("once");
        theCopy = new ACL(theOrig);
    }
    
    /**
     * Testcase for getACLList() (and isEmpty()).
     */
    public void testACLList () {
        ACL empty = new ACL ();
        
        assertTrue(empty.isEmpty());
        assertTrue(empty.getACLList().isEmpty());

        ACL one      = new ACL ("one");
        List theList = one.getACLList();
        assertEquals(1, theList.size());
        assertTrue  (theList.contains("one"));
        
        ACL two      = new ACL (new String[] { "einsch",  "ztschwei"} );
        theList = two.getACLList();
        assertEquals(2, theList.size());
        assertTrue  (theList.contains("einsch"));
        assertTrue  (theList.contains("ztschwei"));
    }

    /**
     * Test, the normal behavior of the ACL.
     */
    public void testHasRoleNormal () {
        ACL theACL = new ACL ("abc,def, hij, klm");

        assertTrue (theACL.hasRole("abc"));
        assertTrue (theACL.hasRole ("def"));
        assertTrue (theACL.hasRole ("hij"));
        assertTrue (theACL.hasRole ("klm"));
        assertFalse (theACL.hasRole ("ab"));
        assertFalse (theACL.hasRole (""));
        assertFalse (theACL.hasRole ((String) null));

        theACL = new ACL ("klm, hij, def,abc ");

        assertTrue (theACL.hasRole ("abc"));
        assertTrue (theACL.hasRole ("def"));
        assertTrue (theACL.hasRole ("hij"));
        assertTrue (theACL.hasRole ("klm"));
        assertTrue (!theACL.hasRole ("ab"));
        assertTrue (!theACL.hasRole (""));
        assertTrue (!theACL.hasRole ((String) null));

        theACL = new ACL ("def, klm, hij, abc ");

        assertTrue (theACL.hasRole ("abc"));
        assertTrue (theACL.hasRole ("def"));
        assertTrue (theACL.hasRole ("hij"));
        assertTrue (theACL.hasRole ("klm"));
        assertTrue (!theACL.hasRole ("ab"));
        assertTrue (!theACL.hasRole (""));
        assertTrue (!theACL.hasRole ((String) null));
    }
    
    /**
     * Test, the normal behavior of the ACL.
     */
    public void testHasAccessNormal () {
        ACL theACL = new ACL ("abc,def, hij, klm");

        assertTrue (theACL.hasAccess ("abc"));
        assertTrue (theACL.hasAccess ("def"));
        assertTrue (theACL.hasAccess ("hij"));
        assertTrue (theACL.hasAccess ("klm"));
        assertTrue (!theACL.hasAccess ("ab"));
        assertTrue (!theACL.hasAccess (""));
        assertTrue (!theACL.hasAccess ((String) null));

        theACL = new ACL ("klm, hij, def,abc ");

        assertTrue (theACL.hasAccess ("abc"));
        assertTrue (theACL.hasAccess ("def"));
        assertTrue (theACL.hasAccess ("hij"));
        assertTrue (theACL.hasAccess ("klm"));
        assertTrue (!theACL.hasAccess ("ab"));
        assertTrue (!theACL.hasAccess (""));
        assertTrue (!theACL.hasAccess ((String) null));

        theACL = new ACL ("def, klm, hij, abc ");

        assertTrue (theACL.hasAccess ("abc"));
        assertTrue (theACL.hasAccess ("def"));
        assertTrue (theACL.hasAccess ("hij"));
        assertTrue (theACL.hasAccess ("klm"));
        assertTrue (!theACL.hasAccess ("ab"));
        assertTrue (!theACL.hasAccess (""));
        assertTrue (!theACL.hasAccess ((String) null));
    }
    /**
     * Test, the normal behavior of the ACL.
     */
    /*
    public void timeTest () {
    	
    	int count = 500;
    	ACL theACL;
    	startTime();
    	for (int i=0; i < count; i++) {
	        theACL = new ACL ("abc,def, hij, klm, nop, qrs, tuv");
	        theACL = new ACL ("klm, tuv, hij, n o p, def,abc ");
	        theACL = new ACL ("def, klm, h i j , qrs, abc, tuv");
	        assertTrue (theACL.hasAccess ("abc"));
    	}
    	super.logTime("creating " +  (count * 3) + " ACLs");
    }
    */

    /**
     * Test Ctor with Array List 
     */
    public void test_checkHasRoleArray () {
    	String list1 [] = {"abc", "def", "hij", "klm"};
    	String list2 [] = {"klm", "hij", "def", "abc"};
    	String list3 [] = {"hij", "abc", "klm", "def"};

        ACL theACL = new ACL (Arrays.asList(list1));

        assertTrue (theACL.hasRole ("abc"));
        assertTrue (theACL.hasRole ("def"));
        assertTrue (theACL.hasRole ("hij"));
        assertTrue (theACL.hasRole ("klm"));
        assertTrue (!theACL.hasRole ("ab"));
        assertTrue (!theACL.hasRole (""));
        assertTrue (!theACL.hasRole ((String) null));

        theACL = new ACL (list2);

        assertTrue (theACL.hasRole ("abc"));
        assertTrue (theACL.hasRole ("def"));
        assertTrue (theACL.hasRole ("hij"));
        assertTrue (theACL.hasRole ("klm"));
        assertTrue (!theACL.hasRole ("ab"));
        assertTrue (!theACL.hasRole (""));
        assertTrue (!theACL.hasRole ((String) null));

        theACL = new ACL (list3);

        assertTrue (theACL.hasRole ("abc"));
        assertTrue (theACL.hasRole ("def"));
        assertTrue (theACL.hasRole ("hij"));
        assertTrue (theACL.hasRole ("klm"));
        assertTrue (!theACL.hasRole ("ab"));
        assertTrue (!theACL.hasRole (""));
        assertTrue (!theACL.hasRole ((String) null));
    }

    /**
     * Test Ctor with Array List 
     */
    public void test_checkHasAccessArray () {
    	String list1 [] = {"abc", "def", "hij", "klm"};
    	String list2 [] = {"klm", "hij", "def", "abc"};
    	String list3 [] = {"hij", "abc", "klm", "def"};

        ACL theACL = new ACL (list1);

        assertTrue (theACL.hasAccess ("abc"));
        assertTrue (theACL.hasAccess ("def"));
        assertTrue (theACL.hasAccess ("hij"));
        assertTrue (theACL.hasAccess ("klm"));
        assertTrue (!theACL.hasAccess ("ab"));
        assertTrue (!theACL.hasAccess (""));
        assertTrue (!theACL.hasAccess ((String) null));

        theACL = new ACL (list2);

        assertTrue (theACL.hasAccess ("abc"));
        assertTrue (theACL.hasAccess ("def"));
        assertTrue (theACL.hasAccess ("hij"));
        assertTrue (theACL.hasAccess ("klm"));
        assertTrue (!theACL.hasAccess ("ab"));
        assertTrue (!theACL.hasAccess (""));
        assertTrue (!theACL.hasAccess ((String) null));

        theACL = new ACL (list3);

        assertTrue (theACL.hasAccess ("abc"));
        assertTrue (theACL.hasAccess ("def"));
        assertTrue (theACL.hasAccess ("hij"));
        assertTrue (theACL.hasAccess ("klm"));
        assertTrue (!theACL.hasAccess ("ab"));
        assertTrue (!theACL.hasAccess (""));
        assertTrue (!theACL.hasAccess ((String) null));
    }
    /**
     * Test Access with other ACLs
     */
    public void test_checkHasAccessACL () {
        ACL theACL = new ACL ("abc,def,hij,klm");

        assertFalse (theACL.hasAccess((ACL) null));
		assertFalse (theACL.hasAccess(new ACL("")));
		assertTrue (theACL.hasAccess (new ACL("def")));
		assertTrue (theACL.hasAccess (new ACL("kkk,def,uuu")));

    }

    /**
     * Test the behavior of the ACL with empty strings.
     */
    public void testHasRoleEmpty () {
        ACL theACL = new ACL("");

		assertFalse (theACL.hasRole ("hij"));
		assertFalse (theACL.hasRole ("ab"));
		assertFalse (theACL.hasRole (""));
		assertFalse (theACL.hasRole ((String) null));
        
        assertNotNull(theACL.toString());
    }
    
	/**
	 * Test the behavior of the ACL with empty strings.
	 */
	public void testHasRoleList () {
		ACL theACL = new ACL("a,b,c,d,e,f,g");

		assertTrue (theACL.hasRole ("g,x"));
		assertTrue (theACL.hasRole ("x,g"));
		assertTrue (theACL.hasRole ("a,c"));
		assertTrue (theACL.hasRole ("g,a,b"));
		assertTrue (theACL.hasRole ("g,d,a"));
        assertFalse (theACL.hasRole("x,y"));
        
		assertNotNull(theACL.toString());
	}
    
    /**
     * Test the access of the ACL with empty strings.
     */
    public void testHasAccessEmpty () {
        ACL theACL = new ACL ("");

        assertTrue (theACL.hasAccess ("hij"));
        assertTrue (theACL.hasAccess ("ab"));
        assertTrue (theACL.hasAccess (""));
        assertTrue (theACL.hasAccess ((String) null));
    }

    /**
     * Test the behavior of the ACL with null parameter.
     */
    public void testHasRoleNull () {
        ACL theACL = new ACL ((String) null);

        assertTrue (!theACL.hasRole ("hij"));
        assertTrue (!theACL.hasRole ("ab"));
        assertTrue (!theACL.hasRole (""));
        assertTrue (!theACL.hasRole ((String) null));
    }

    /**
     * Test the access of the ACL with null parameter.
     */
    public void testHasAccessNull () {
        ACL theACL = new ACL ((String) null);

        assertTrue (theACL.hasAccess ("hij"));
        assertTrue (theACL.hasAccess ("ab"));
        assertTrue (theACL.hasAccess (""));
        assertTrue (theACL.hasAccess ((String) null));
    }

    /**
     * Test Adding roles
     */
    public void test_checkAddRole () {
        ACL theACL = new ACL ((String) null);

        assertEquals ("", theACL.getACLString());
        assertTrue   (theACL.addRole("qqq"));
        assertEquals ("qqq", theACL.getACLString());
        assertTrue   (theACL.addRole("aaa"));
        assertTrue   ("'aaa' is not in the ACL", theACL.getACLString().indexOf("aaa") >= 0);
        assertTrue   ("'qqq' is not in the ACL", theACL.getACLString().indexOf("qqq") >= 0);
        assertTrue   ("',' is not in the ACL", theACL.getACLString().indexOf(",") >= 0);
        assertTrue   (theACL.addRole("bbb"));
        assertTrue   ("'aaa' is not in the ACL", theACL.getACLString().indexOf("aaa") >= 0);
        assertTrue   ("'bbb' is not in the ACL", theACL.getACLString().indexOf("bbb") >= 0);
        assertTrue   ("'qqq' is not in the ACL", theACL.getACLString().indexOf("qqq") >= 0);
        assertTrue   ("',' is not in the ACL", theACL.getACLString().indexOf(",") >= 0);
        assertTrue   (theACL.addRole("xxx"));
        assertTrue   ("'aaa' is not in the ACL", theACL.getACLString().indexOf("aaa") >= 0);
        assertTrue   ("'bbb' is not in the ACL", theACL.getACLString().indexOf("bbb") >= 0);
        assertTrue   ("'qqq' is not in the ACL", theACL.getACLString().indexOf("qqq") >= 0);
        assertTrue   ("'xxx' is not in the ACL", theACL.getACLString().indexOf("xxx") >= 0);
        assertTrue   ("',' is not in the ACL", theACL.getACLString().indexOf(",") >= 0);
        assertTrue   (!theACL.addRole("bbb"));
    }

    public void testHardcore() throws Exception {
        Random theRand = new Random(0xDEADBEAF);
        ACL[]  theACLs = new ACL[100];

        this.startTime();

        this.initRoles(theRand);

        for (int thePos = 0; thePos < theACLs.length; thePos++) {
            theACLs[thePos] = this.createTestACL(theRand);
        }

        this.logTime("Creating " + theACLs.length + " ACLs");

        int thePos1;
        int thePos2;
        int theCount = 50000;

        for (int thePos = 0; thePos < theCount; thePos++) {
            thePos1 = theRand.nextInt(theACLs.length);
            thePos2 = theRand.nextInt(theACLs.length);
            theACLs[thePos1].hasAccess(theACLs[thePos2]);
        }

        this.logTime("testing " + theCount + " hasAccess");

        for (int thePos = 0; thePos < theCount; thePos++) {
            thePos1 = theRand.nextInt(theACLs.length);
            thePos2 = theRand.nextInt(theACLs.length);
            theACLs[thePos1].hasRole(theACLs[thePos2]);
        }

        this.logTime("testing " + theCount + " hasRole");
        this.logTime("All Tests completed");
    }

    public void testNewSearch() throws Exception {
        Random theRand = new Random(0xDEADBEAF);
        ACL[]  theACLs = new ACL[100];
    	//string which holds the Stringrights
    	String demoACL;

        this.startTime();
 
    	demoACL = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,"
    			+ "aa,ab,ac,ad,ae,af,ag,ah,ai,aj,ak,al,am,an,ao,ap,aq,ar,as,at,au,av,aw,ax,ay,az,"
    			+ "ba,bb,bc,bd,be,bf,bg,bh,bi,bj,bk,bl,bm,bn,bo,bp,bq,br,bs,bt,bu,bv,bw,bx,by,bz,"
    			+ "ca,cb,cc,cd,ce,cf,cg,ch,ci,cj,ck,cl,cm,cn,co,cp,cq,cr,cs,ct,cu,cv,cw,cx,cy,cz,"
    			+ "da,db,dc,dd,de,df,dg,dh,di,dj,dk,dl,dm,dn,do,dp,dq,dr,ds,dt,du,dv,dw,dx,dy,dz,"
    			+ "ea,eb,ec,ed,ee,ef,eg,eh,ei,ej,ek,el,em,en,eo,ep,eq,er,es,et,eu,ev,ew,ex,ey,ez,"
    			+ "fa,fb,fc,fd,fe,ff,fg,fh,fi,fj,fk,fl,fm,fn,fo,fp,fq,fr,fs,ft,fu,fv,fw,fx,fy,fz,"
    			+ "ga,gb,gc,gd,ge,gf,gg,gh,gi,gj,gk,gl,gm,gn,go,gp,gq,gr,gs,gt,gu,gv,gw,gx,gy,gz,"
    			+ "ha,hb,hc,hd,he,hf,hg,hh,hi,hj,hk,hl,hm,hn,ho,hp,hq,hr,hs,ht,hu,hv,hw,hx,hy,hz,"
    			+ "ia,ib,ic,id,ie,if,ig,ih,ii,ij,ik,il,im,in,io,ip,iq,ir,is,it,iu,iv,iw,ix,iy,iz,"
    			+ "ja,jb,jc,jd,je,jf,jg,jh,ji,jj,jk,jl,jm,jn,jo,jp,jq,jr,js,jt,ju,jv,jw,jx,jy,jz,"
    			+ "ka,kb,kc,kd,ke,kf,kg,kh,ki,kj,kk,kl,km,kn,ko,kp,kq,kr,ks,kt,ku,kv,kw,kx,ky,kz,"
    			+ "la,lb,lc,ld,le,lf,lg,lh,li,lj,lk,ll,lm,ln,lo,lp,lq,lr,ls,lt,lu,lv,lw,lx,ly,lz,"
    			+ "ma,mb,mc,md,me,mf,mg,mh,mi,mj,mk,ml,mm,mn,mo,mp,mq,mr,ms,mt,mu,mv,mw,mx,my,mz,"
    			+ "na,nb,nc,nd,ne,nf,ng,nh,ni,nj,nk,nl,nm,nn,no,np,nq,nr,ns,nt,nu,nv,nw,nx,ny,nz,"
    			+ "oa,ob,oc,od,oe,of,og,oh,oi,oj,ok,ol,om,on,oo,op,oq,or,os,ot,ou,ov,ow,ox,oy,oz,"
    			+ "pa,pb,pc,pd,pe,pf,pg,ph,pi,pj,pk,pl,pm,pn,po,pp,pq,pr,ps,pt,pu,pv,pw,px,py,pz,"
    			+ "qa,qb,qc,qd,qe,qf,qg,qh,qi,qj,qk,ql,qm,qn,qo,qq,qq,qr,qs,qt,qu,qv,qw,qx,qy,qz,"    			
    			+ "ra,rb,rc,rd,re,rf,rg,rh,rr,rr,rk,rl,rm,rn,ro,rp,rq,rr,rs,rt,ru,rv,rw,rx,ry,rz,"
    			+ "ta,tb,tc,td,te,tf,tg,th,tt,tt,tk,tl,tm,tn,to,tp,tq,tr,ts,tt,tu,tv,tw,tx,ty,tz,"
    			+ "ua,ub,uc,ud,ue,uf,ug,uh,uu,uu,uk,ul,um,un,uo,up,uq,ur,us,ut,uu,uv,uw,ux,uy,uz,"
    			+ "va,vb,vc,vd,ve,vf,vg,vh,vv,vv,vk,vl,vm,vn,vo,vp,vq,vr,vs,vt,vu,vv,vw,vx,vy,vz,"
    			+ "wa,wb,wc,wd,we,wf,wg,wh,ww,ww,wk,wl,wm,wn,wo,wp,wq,wr,ws,wt,wu,wv,ww,wx,wy,wz,"
    			+ "xa,xb,xc,xd,xe,xf,xg,xh,xx,xx,xk,xl,xm,xn,xo,xp,xq,xr,xs,xt,xu,xv,xw,xx,xy,xz,"
    			+ "ya,yb,yc,yd,ye,yf,yg,yh,yy,yy,yk,yl,ym,yn,yo,yp,yq,yr,ys,yt,yu,yv,yw,yx,yy,yz,"
    			+ "za,zb,zc,zd,ze,zf,zg,zh,zz,zz,zk,zl,zm,zn,zo,zp,zq,zr,zs,zt,zu,zv,zw,zx,zz,zz";

        int demoACLlength = demoACL.length();
        
        for (int thePos = 0; thePos < theACLs.length; thePos++) {
            theACLs[thePos] = new ACL (demoACL.substring(theRand.nextInt(demoACLlength)));
        }

        this.logTime("Creating " + theACLs.length + " ACLs");
        int thePos1;
        int thePos2;
        int theCount = 50000;

        for (int thePos = 0; thePos < theCount; thePos++) {
            thePos1 = theRand.nextInt(theACLs.length);
            thePos2 = theRand.nextInt(theACLs.length);
            theACLs[thePos1].hasAccess(theACLs[thePos2]);
        }

        this.logTime("testing " + theCount + " hasAccess");

        for (int thePos = 0; thePos < theCount; thePos++) {
            thePos1 = theRand.nextInt(theACLs.length);
            thePos2 = theRand.nextInt(theACLs.length);
            theACLs[thePos1].hasRole(theACLs[thePos2]);
        }

        this.logTime("testing " + theCount + " hasRole");
        this.logTime("All Tests completed");



        this.logTime("Finished Testing new hasRole(int[])");
    }


    public void testRealData() throws Exception {
        Random theRand		= new Random(0xDEADBEAF);
        ACL[]  theACLs		= new ACL[1000];
    	//string which holds the Stringrights
    	String[] demoACL	= new String[116];
    	String dummyACL = "";
    	int thisACLStart	= 0;
    	int thisACLStop	= 0;

        this.startTime();
 
		//the crypted Projectnames are replaced by dummy Names
		demoACL[0] = "tl.admin";
		demoACL[1] = "25ZollBEreifung.ProjektAdmin"; 
		demoACL[2] = "25ZollBEreifung.accessAll"; 
		demoACL[3] = "25ZollBEreifung.read.full"; 
		demoACL[4] = "25ZollBEreifung.read.restricted"; 
		demoACL[5] = "25ZollBEreifung.write.full"; 
		demoACL[6] = "25ZollBEreifung.write.restricted"; 
		demoACL[7] = "AMG32CDI.ProjektAdmin"; 
		demoACL[8] = "AMG32CDI.accessAll"; 
		demoACL[9] = "AMG32CDI.read.full"; 
		demoACL[10] = "AMG32CDI.read.restricted"; 
		demoACL[11] = "AMG32CDI.write.full"; 
		demoACL[12] = "AMG32CDI.write.restricted"; 
		demoACL[13] = "AbstractesSprachKontrollSystem.ProjektAdmin"; 
		demoACL[14] = "AbstractesSprachKontrollSystem.accessAll"; 
		demoACL[15] = "AbstractesSprachKontrollSystem.minion user"; 
		demoACL[16] = "AbstractesSprachKontrollSystem.read.full"; 
		demoACL[17] = "AbstractesSprachKontrollSystem.read.restricted"; 
		demoACL[18] = "AbstractesSprachKontrollSystem.write.full"; 
		demoACL[19] = "AbstractesSprachKontrollSystem.write.restricted"; 
		demoACL[20] = "AdaptiveGetriebeSteuerung.ProjektAdmin"; 
		demoACL[21] = "AdaptiveGetriebeSteuerung.accessAll"; 
		demoACL[22] = "AdaptiveGetriebeSteuerung.read.full"; 
		demoACL[23] = "AdaptiveGetriebeSteuerung.read.restricted"; 
		demoACL[24] = "AdaptiveGetriebeSteuerung.write.full"; 
		demoACL[25] = "AdaptiveGetriebeSteuerung.write.restricted"; 
		demoACL[26] = "BiXenonScheinwerfer.ProjektAdmin"; 
		demoACL[27] = "BiXenonScheinwerfer.accessAll"; 
		demoACL[28] = "BiXenonScheinwerfer.read.full"; 
		demoACL[29] = "BiXenonScheinwerfer.read.restricted"; 
		demoACL[30] = "BiXenonScheinwerfer.write.full"; 
		demoACL[31] = "BiXenonScheinwerfer.write.restricted"; 
		demoACL[32] = "CockpitDesignMaybach.ProjektAdmin"; 
		demoACL[33] = "CockpitDesignMaybach.TestRolle"; 
		demoACL[34] = "CockpitDesignMaybach.accessAll"; 
		demoACL[35] = "CockpitDesignMaybach.accessPMA"; 
		demoACL[36] = "CockpitDesignMaybach.accessPMB"; 
		demoACL[37] = "CockpitDesignMaybach.accessPMC"; 
		demoACL[38] = "CockpitDesignMaybach.accessPMD"; 
		demoACL[39] = "CockpitDesignMaybach.read.full"; 
		demoACL[40] = "CockpitDesignMaybach.read.restricted"; 
		demoACL[41] = "CockpitDesignMaybach.write.full"; 
		demoACL[42] = "CockpitDesignMaybach.write.restricted"; 
		demoACL[43] = "Feinkonzept.ProjektAdmin"; 
		demoACL[44] = "Feinkonzept.accessAll"; 
		demoACL[45] = "Feinkonzept.read.full"; 
		demoACL[46] = "Feinkonzept.read.restricted"; 
		demoACL[47] = "Feinkonzept.write.full"; 
		demoACL[48] = "Feinkonzept.write.restricted"; 
		demoACL[49] = "GrobkonzeptTeilprojekt1.ProjektAdmin"; 
		demoACL[50] = "GrobkonzeptTeilprojekt1.accessAll"; 
		demoACL[51] = "GrobkonzeptTeilprojekt1.read.full"; 
		demoACL[52] = "GrobkonzeptTeilprojekt1.read.restricted"; 
		demoACL[53] = "GrobkonzeptTeilprojekt1.write.full"; 
		demoACL[54] = "GrobkonzeptTeilprojekt1.write.restricted"; 
		demoACL[55] = "GrobkonzeptTeilprojekt2.1"; 
		demoACL[56] = "GrobkonzeptTeilprojekt2.ProjektAdmin"; 
		demoACL[57] = "GrobkonzeptTeilprojekt2.accessAll"; 
		demoACL[58] = "GrobkonzeptTeilprojekt2.demo BMA"; 
		demoACL[59] = "GrobkonzeptTeilprojekt2.demo"; 
		demoACL[60] = "GrobkonzeptTeilprojekt2.read.full"; 
		demoACL[61] = "GrobkonzeptTeilprojekt2.read.restricted"; 
		demoACL[62] = "GrobkonzeptTeilprojekt2.write.full"; 
		demoACL[63] = "GrobkonzeptTeilprojekt2.write.restricted"; 
		demoACL[64] = "InfrarotAbstandsautomatik.ProjektAdmin"; 
		demoACL[65] = "InfrarotAbstandsautomatik.accessAll"; 
		demoACL[66] = "InfrarotAbstandsautomatik.read.full"; 
		demoACL[67] = "InfrarotAbstandsautomatik.read.restricted"; 
		demoACL[68] = "InfrarotAbstandsautomatik.write.full"; 
		demoACL[69] = "InfrarotAbstandsautomatik.write.restricted"; 
		demoACL[70] = "LinguistischesSicherungsSystem.ProjektAdmin"; 
		demoACL[71] = "LinguistischesSicherungsSystem.accessAll"; 
		demoACL[72] = "LinguistischesSicherungsSystem.read.full"; 
		demoACL[73] = "LinguistischesSicherungsSystem.read.restricted"; 
		demoACL[74] = "LinguistischesSicherungsSystem.write.full"; 
		demoACL[75] = "LinguistischesSicherungsSystem.write.restricted"; 
		demoACL[76] = "MetaGenerizitaetsGenerator.ProjektAdmin"; 
		demoACL[77] = "MetaGenerizitaetsGenerator.accessAll"; 
		demoACL[78] = "MetaGenerizitaetsGenerator.read.full"; 
		demoACL[79] = "MetaGenerizitaetsGenerator.read.restricted"; 
		demoACL[80] = "MetaGenerizitaetsGenerator.write.full"; 
		demoACL[81] = "MetaGenerizitaetsGenerator.write.restricted"; 
		demoACL[82] = "MultiFunktionalesInduktionsSystem.Controling"; 
		demoACL[83] = "MultiFunktionalesInduktionsSystem.ProjektAdmin"; 
		demoACL[84] = "MultiFunktionalesInduktionsSystem.accessAll"; 
		demoACL[85] = "MultiFunktionalesInduktionsSystem.read.full"; 
		demoACL[86] = "MultiFunktionalesInduktionsSystem.read.restricted"; 
		demoACL[87] = "MultiFunktionalesInduktionsSystem.write.full"; 
		demoACL[88] = "MultiFunktionalesInduktionsSystem.write.restricted"; 
		demoACL[89] = "MultilenkerHinterachse.ProjektAdmin"; 
		demoACL[90] = "MultilenkerHinterachse.accessAll"; 
		demoACL[91] = "MultilenkerHinterachse.read.full"; 
		demoACL[92] = "MultilenkerHinterachse.read.restricted"; 
		demoACL[93] = "MultilenkerHinterachse.write.full"; 
		demoACL[94] = "MultilenkerHinterachse.write.restricted"; 
		demoACL[95] = "SitzlueftungS-Klasse.ProjektAdmin"; 
		demoACL[96] = "SitzlueftungS-Klasse.accessAll"; 
		demoACL[97] = "SitzlueftungS-Klasse.read.full"; 
		demoACL[98] = "SitzlueftungS-Klasse.read.restricted"; 
		demoACL[99] = "SitzlueftungS-Klasse.write.full"; 
		demoACL[100] = "SitzlueftungS-Klasse.write.restricted"; 
		demoACL[101] = "SperrdifferntialMitHALDEXKupplung.ProjektAdmin"; 
		demoACL[102] = "SperrdifferntialMitHALDEXKupplung.accessAll"; 
		demoACL[103] = "SperrdifferntialMitHALDEXKupplung.read.full"; 
		demoACL[104] = "SperrdifferntialMitHALDEXKupplung.read.restricted"; 
		demoACL[105] = "SperrdifferntialMitHALDEXKupplung.write.full"; 
		demoACL[106] = "SperrdifferntialMitHALDEXKupplung.write.restricted"; 
		demoACL[107] = "Superkonzept.ProjektAdmin"; 
		demoACL[108] = "Superkonzept.accessAll"; 
		demoACL[109] = "Superkonzept.read.full"; 
		demoACL[110] = "Superkonzept.read.restricted"; 
		demoACL[111] = "Superkonzept.write.full"; 
		demoACL[112] = "Superkonzept.write.restricted"; 
		demoACL[113] = "gl.admin"; 
		demoACL[114] = "tech.admin";
		demoACL[115] = ".all";

        
        for (int thePos = 0; thePos < theACLs.length; thePos++) {
			thisACLStart = theRand.nextInt(demoACL.length);
			thisACLStop  = theRand.nextInt(demoACL.length);
			
			if (thisACLStart < thisACLStop)
				for (int tp=thisACLStart; tp < thisACLStop; tp++){ dummyACL = dummyACL + demoACL[tp]; }
			else if (thisACLStart < thisACLStop)
				for (int tp=thisACLStop; tp < thisACLStart; tp++){ dummyACL = dummyACL + demoACL[tp]; }
			else
				{ dummyACL = demoACL[thisACLStop]; }

            theACLs[thePos] = new ACL (dummyACL);
        }

        this.logTime("Creating " + theACLs.length + " ACLs");
        int thePos1;
        int thePos2;
        int theCount = 50000;

        for (int thePos = 0; thePos < theCount; thePos++) {
            thePos1 = theRand.nextInt(theACLs.length);
            thePos2 = theRand.nextInt(theACLs.length);
            theACLs[thePos1].hasAccess(theACLs[thePos2]);
        }

        this.logTime("testing " + theCount + " hasAccess");

        for (int thePos = 0; thePos < theCount; thePos++) {
            thePos1 = theRand.nextInt(theACLs.length);
            thePos2 = theRand.nextInt(theACLs.length);
            theACLs[thePos1].hasRole(theACLs[thePos2]);
        }

        this.logTime("testing " + theCount + " hasRole");

        this.logTime("All Tests completed");
    }
    
    /** Special Effect that happend when using the Remote KB */
    public void testRemote() {
        ACL theACL    = new ACL("admin,user");
        ACL theRoles  = new ACL("user,admin,R.admin");
        
        assertTrue(theACL.hasAccess(theRoles));
    }

    /** Test adding/merging of ACL (and equals(), too). */
    public void testAddACL() {
        ACL acl1    = new ACL("alpha,bravo,charlie,delta");
        ACL empty   = new ACL();
        assertTrue(! acl1.add(empty));
        assertEquals(acl1, acl1);
        assertEquals("alpha,bravo,charlie,delta", acl1.getACLString());
        assertTrue(!acl1.equals(empty));
        assertTrue(!empty.equals(acl1));
        assertTrue( empty.equals(empty));

        ACL result  = new ACL();
        assertTrue(result.add(acl1));
        assertTrue(result.equals(acl1));
        assertEquals("alpha,bravo,charlie,delta", result.getACLString());
        assertTrue(acl1.equals(result));

        ACL acl2    = new ACL("charlie,alpha,foxtrott,golf,bravo");
        assertTrue(acl2.add(acl1));
        ACL acl3    = new ACL("alpha,bravo,charlie,delta,foxtrott,golf");
        assertEquals("alpha,bravo,charlie,delta,foxtrott,golf", acl2.getACLString());

        assertTrue(acl2.equals(acl3));
        assertTrue(acl3.equals(acl2));
        
        assertTrue(!acl2.equals(acl1));
        assertTrue(!acl1.equals(acl2));
    }

	public void testEquals() {
		ACL acl1 = new ACL("alpha,bravo,charlie,delta");
		assertEquals(acl1, acl1);
		assertEquals(acl1.hashCode(), acl1.hashCode());

		ACL acl1Clone = new ACL(acl1);
		assertEquals(acl1, acl1Clone);
		assertEquals(acl1.hashCode(), acl1Clone.hashCode());

		ACL empty = new ACL();
		ACL empty2 = new ACL();
		assertEquals(empty, empty2);
		assertEquals(empty.hashCode(), empty2.hashCode());
	}

    protected void initRoles(Random aRand) {
        for (int thePos = 0; thePos < this.roles.length; thePos++) {
            this.roles[thePos] = "pos.project." + aRand.nextFloat();
        }
    }

    protected ACL createTestACL(Random aRand) {
        int    theSize   = aRand.nextInt(this.roles.length);
        String theACLs[] = new String[theSize];

        for (int thePos = 0; thePos < theSize; thePos++) {
            theACLs[thePos] = this.roles[aRand.nextInt(this.roles.length)];
        }

        // Logger.debug("Created ACL with " + theSize + " values.", this);

        return new ACL(theACLs);
        
    }

    /**
     * Used for framework.
     *
     * @return    The test suite for this class.
     */
    public static Test suite () {
        //return new TLTestSetup(new TestSuite (TestACL.class));
		return TLTestSetup.createTLTestSetup(TestACL.class);
        // return new TLTestSetup(new TestACL ("testAddACL"));
    }

    /**
     * Main class to start test without UI.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
    	// SHOW_TIME = true;
    	
    	junit.textui.TestRunner.run (suite ());
    }
}
