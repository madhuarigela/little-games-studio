package com.madhuarigela.slicegalaxy;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(9, 13, 35));
        getWindow().setNavigationBarColor(Color.rgb(9, 13, 35));
        setContentView(new GameView(this));
    }

    static class GameView extends View {
        final Paint p = new Paint(3);
        final Handler handler = new Handler(Looper.getMainLooper());
        final Random random = new Random();
        final ArrayList<Piece> pieces = new ArrayList<>();
        final ArrayList<Slash> slashes = new ArrayList<>();
        final int[] fruitColors = {Color.rgb(255,91,105), Color.rgb(255,184,64), Color.rgb(101,220,147), Color.rgb(107,177,255), Color.rgb(197,132,255)};
        long lastFrame, lastSpawn, startTime, lastSwipe, pauseStartedAt;
        int score = 0, lives = 3, best = 0;
        boolean started = false, ended = false, paused = false, backgroundPaused = false;
        final float density;
        float fingerX, fingerY;
        final Runnable ticker = new Runnable() { public void run() { invalidate(); handler.postDelayed(this, 16); } };

        GameView(Context c) { super(c); density=getResources().getDisplayMetrics().density; best=c.getSharedPreferences("slice_galaxy", Context.MODE_PRIVATE).getInt("best", 0); setContentDescription("Slice Galaxy arcade game. Swipe across fruit to score points and avoid bombs."); setLayerType(View.LAYER_TYPE_SOFTWARE, null); handler.post(ticker); setFocusable(true); }
        void startGame() { pieces.clear(); slashes.clear(); score=0; lives=3; started=true; ended=false; paused=false; startTime=System.currentTimeMillis(); lastFrame=startTime; lastSpawn=startTime; lastSwipe=0; backgroundPaused=false; invalidate(); }
        void spawn() {
            float w=getWidth(), h=getHeight();
            boolean bomb=random.nextFloat()<0.16f;
            float r=Math.min(w,h)*0.045f;
            float x=r+random.nextFloat()*(w-2*r);
            pieces.add(new Piece(x,h+30,r,bomb?Color.rgb(45,49,65):fruitColors[random.nextInt(fruitColors.length)],bomb,(random.nextFloat()-.5f)*2.2f,-(h*.65f+random.nextFloat()*h*.32f)));
        }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            long now=System.currentTimeMillis();
            c.save(); c.scale(density,density);
            float w=getWidth()/density,h=getHeight()/density;
            c.drawColor(Color.rgb(9,13,35));
            p.setShader(new android.graphics.LinearGradient(0,0,w,h,Color.rgb(18,29,67),Color.rgb(10,13,38),android.graphics.Shader.TileMode.CLAMP));
            c.drawRect(0,0,w,h,p); p.setShader(null);
            p.setColor(Color.argb(75,210,225,255));
            for(int i=0;i<28;i++){ float x=(i*97)%Math.max(1,(int)w); float y=(i*173)%Math.max(1,(int)h); c.drawCircle(x,y,1.6f,p); }

            if(started && !ended && !paused) {
                float dt=Math.min(0.035f,(now-lastFrame)/1000f); lastFrame=now;
                if(now-lastSpawn>650){ spawn(); if(random.nextFloat()<.25f) spawn(); lastSpawn=now; }
                long remain=60000-(now-startTime);
                if(remain<=0){finishRound();}
                Iterator<Piece> it=pieces.iterator();
                while(it.hasNext()){
                    Piece a=it.next(); a.x+=a.vx*dt*60; a.y+=a.vy*dt*60; a.vy+=0.075f*dt*60;
                    if(a.y>h+100){ if(!a.bomb) lives--; it.remove(); if(lives<=0){finishRound();} }
                    else drawPiece(c,a);
                }
                for(Slash s:slashes){p.setColor(Color.argb(180,125,245,255));p.setStrokeWidth(7);c.drawLine(s.x1,s.y1,s.x2,s.y2,p);}
                Iterator<Slash> slashIterator=slashes.iterator();
                while(slashIterator.hasNext()){ if(now-slashIterator.next().time>120) slashIterator.remove(); }
                drawHud(c,w,remain);
            } else if(!started) {
                text(c,"SLICE",w/2,h*.25f,72,Color.WHITE,true);
                text(c,"GALAXY",w/2,h*.34f,42,Color.rgb(112,220,255),true);
                text(c,"SWIPE FRUIT  •  AVOID BOMBS",w/2,h*.49f,16,Color.rgb(195,207,237),true);
                button(c,w/2,h*.67f,"PLAY");
                text(c,"60 SECOND ARCADE CHALLENGE",w/2,h*.77f,13,Color.rgb(139,155,195),false);
            } else if(paused) {
                text(c,"PAUSED",w/2,h*.38f,42,Color.WHITE,true);
                button(c,w/2,h*.56f,"RESUME");
                button(c,w/2,h*.68f,"RESTART");
            } else {
                text(c,"ROUND COMPLETE",w/2,h*.29f,28,Color.WHITE,true);
                text(c,""+score,w/2,h*.43f,70,Color.rgb(112,220,255),true);
                text(c,"BEST  "+best,w/2,h*.51f,17,Color.rgb(196,208,239),true);
                button(c,w/2,h*.66f,"PLAY AGAIN");
                text(c,"GOOD CUTS. TRY FOR A HIGHER SCORE.",w/2,h*.76f,12,Color.rgb(139,155,195),false);
            }
            c.restore();
        }
        void drawPiece(Canvas c, Piece a){
            p.setColor(a.color); p.setStyle(Paint.Style.FILL); c.drawCircle(a.x,a.y,a.r,p);
            if(a.bomb){p.setColor(Color.WHITE);p.setStrokeWidth(4);c.drawLine(a.x-a.r*.35f,a.y-a.r*.35f,a.x+a.r*.35f,a.y+a.r*.35f,p);c.drawLine(a.x+a.r*.35f,a.y-a.r*.35f,a.x-a.r*.35f,a.y+a.r*.35f,p);}
            else {p.setColor(Color.argb(140,255,255,255));c.drawOval(new RectF(a.x-a.r*.45f,a.y-a.r*.65f,a.x-a.r*.05f,a.y-a.r*.2f),p);p.setColor(Color.rgb(74,170,103));c.drawOval(new RectF(a.x-3,a.y-a.r-9,a.x+7,a.y-a.r+3),p);}
        }
        void drawHud(Canvas c,float w,long remain){
            text(c,"SCORE  "+score,28,48,18,Color.WHITE,false);
            String hearts="";
            for(int i=0;i<Math.max(0,lives);i++) hearts+="♥ ";
            text(c,hearts,w-35,48,18,Color.rgb(255,111,135),true);
            text(c,String.format(java.util.Locale.US,"%02d",Math.max(0,remain/1000)),w/2,65,26,Color.rgb(122,225,255),true);
            text(c,"PAUSE",w-48,95,12,Color.rgb(180,198,235),true);
        }
        void text(Canvas c,String s,float x,float y,float size,int color,boolean center){
            p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));p.setTextAlign(center?Paint.Align.CENTER:Paint.Align.LEFT);c.drawText(s,x,y,p);
        }
        void button(Canvas c,float x,float y,String label){
            p.setColor(Color.rgb(69,211,178));c.drawRoundRect(new RectF(x-108,y-29,x+108,y+29),26,26,p);
            text(c,label,x,y+7,18,Color.rgb(8,35,43),true);
        }
        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX()/density,y=e.getY()/density;
            if(e.getAction()==MotionEvent.ACTION_DOWN){fingerX=x;fingerY=y;return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE && started && !ended && !paused){
                if(Math.hypot(x-fingerX,y-fingerY)>14){cut(fingerX,fingerY,x,y);fingerX=x;fingerY=y;} return true;
            }
            if(e.getAction()==MotionEvent.ACTION_UP){
                float w=getWidth(),h=getHeight();
                if(!started || ended){startGame();return true;}
                if(paused){if(y>h*.62f)startGame();else {startTime+=System.currentTimeMillis()-pauseStartedAt; paused=false; lastFrame=System.currentTimeMillis();} invalidate();return true;}
                if(y<125 && x>w*.72f){paused=true;pauseStartedAt=System.currentTimeMillis();invalidate();return true;}
                cut(fingerX,fingerY,x,y);return true;
            }
            return true;
        }
        void finishRound(){ ended=true; best=Math.max(best,score); getContext().getSharedPreferences("slice_galaxy", Context.MODE_PRIVATE).edit().putInt("best",best).apply(); }
        void cut(float x1,float y1,float x2,float y2){
            long now=System.currentTimeMillis();
            slashes.add(new Slash(x1,y1,x2,y2,now));
            Iterator<Piece> it=pieces.iterator();
            while(it.hasNext()){
                Piece a=it.next();
                if(distance(a.x,a.y,x1,y1,x2,y2)<a.r+13){
                    if(a.bomb){lives=0;finishRound();}
                    else {score+=10+(now-lastSwipe<500?5:0);lastSwipe=now;}
                    it.remove();
                }
            }
        }
        float distance(float px,float py,float x1,float y1,float x2,float y2){
            float dx=x2-x1,dy=y2-y1;
            if(dx==0&&dy==0)return (float)Math.hypot(px-x1,py-y1);
            float t=Math.max(0,Math.min(1,((px-x1)*dx+(py-y1)*dy)/(dx*dx+dy*dy)));
            return (float)Math.hypot(px-(x1+t*dx),py-(y1+t*dy));
        }
        @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility); if(visibility!=View.VISIBLE && started && !ended && !paused){paused=true;backgroundPaused=true;pauseStartedAt=System.currentTimeMillis();} else if(visibility==View.VISIBLE && paused && backgroundPaused && pauseStartedAt>0){startTime+=System.currentTimeMillis()-pauseStartedAt;pauseStartedAt=0;paused=false;backgroundPaused=false;lastFrame=System.currentTimeMillis();}}
        @Override protected void onDetachedFromWindow(){handler.removeCallbacks(ticker);super.onDetachedFromWindow();}
        static class Piece {float x,y,r,vx,vy;int color;boolean bomb;Piece(float x,float y,float r,int color,boolean bomb,float vx,float vy){this.x=x;this.y=y;this.r=r;this.color=color;this.bomb=bomb;this.vx=vx;this.vy=vy;}}
        static class Slash {float x1,y1,x2,y2;long time;Slash(float a,float b,float c,float d,long t){x1=a;y1=b;x2=c;y2=d;time=t;}}
    }
}
