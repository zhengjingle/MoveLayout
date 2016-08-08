package com.zjl.customview;

import java.util.LinkedList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
/**
 * @author ZhengJingle
 */

/*
 * MoveLayout，移动布局，实现控件联动，一个主动移动view，对应多个被动移动views
 */
public class MoveLayout extends FrameLayout{
	private View activeView;//主动移动view
	private boolean isStraight=true;//主动移动view是否走直线
	private double distance=0;//主动view移动的距离
	private double distanceX,distanceY;////主动view移动在x,y轴上的距离
	private boolean useMoveArea;//主动view使用移动区域
	private int minX,maxX,minY,maxY;//主动view移动区域
	private int fromX,fromY;//主动view开始坐标
	private int toX,toY;//主动view目标坐标
	private double k=0,b=0;//主动view直线的k和b
	private double kd=0,bd=0;//垂直于主动view直线的k和b
	final int X=1;//X轴移动
	final int Y=2;//Y轴移动
	final int XY=3;//XY轴移动
	private int mode=XY;
	private double movingPrecent=0;//移动百分比
	
	private LinkedList<PassiveMoveItem> pmiList=new LinkedList<PassiveMoveItem>();//被动移动views
	
	public MoveLayout(Context context) {
		super(context);
		// TODO 自动生成的构造函数存根
	}

	public MoveLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO 自动生成的构造函数存根
	}

	public MoveLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO 自动生成的构造函数存根
	}
	/**
	 * 设置主动view的移动区域，闭区间
	 * @param useMoveArea true使用；false不使用，后面的参数不起作用 
	 * @param minX x轴上的最小值
	 * @param maxX x轴上的最大值
	 * @param minY y轴上的最小值
	 * @param maxY y轴上的最大值
	 */
	public void setActiveMoveArea(boolean useMoveArea,int minX,int maxX,int minY,int maxY){
		this.useMoveArea=useMoveArea;
		this.minX=minX;
		this.minY=minY;
		this.maxX=maxX;
		this.maxY=maxY;
	}
	
	/**
	 * 移动到百分比对应的位置。如果主动view不走直线，则自动设为走直线上的位置。
	 * @param precent 0.0——0%，1.0——100%，依此类推
	 */
	public void moveToPrecent(double precent){
		this.movingPrecent=precent;
		runActiveMoveItem();
		runPassiveMoveItem();
		
		if(myOnMovingListener!=null){
			myOnMovingListener.onMoving(movingPrecent);
		}
	}
	
	/**
	 * 设置主动移动view
	 * @param view 主动移动view
	 * @param toX 目标x坐标
	 * @param toY 目标y坐标
	 * @param isStraight true走直线；false不走直线，当计算movingPrecent时，用主动移动view移动时左上角的坐标投影到直线上的坐标计算
	 */
	public void setActiveMoveItem(View view,int toX,int toY,boolean isStraight){
		view.setOnTouchListener(new MoveItemTouchListener(view,toX,toY));
		this.activeView=view;
		this.isStraight=isStraight;
	}
	
	/**
	 * 设置被动移动view
	 * @param view 被动移动view
	 * @param toX 目标x坐标
	 * @param toY 目标y坐标
	 */
	public void setPassiveMoveItem(View view,int toX,int toY){
		PassiveMoveItem pmi=new PassiveMoveItem(view,toX,toY);
		pmiList.add(pmi);
	}
	
	private void passiveMoveItemInit(){
		for(PassiveMoveItem pmi:pmiList){
			
			FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) pmi.view.getLayoutParams();
			layoutParams.gravity=-1;//清除所有gravity布局
			layoutParams.leftMargin=pmi.view.getLeft();
			layoutParams.topMargin=pmi.view.getTop();
			pmi.view.setLayoutParams(layoutParams);
			
			//初始化
			pmi.fromX=pmi.view.getLeft();
			pmi.fromY=pmi.view.getTop();
			
			pmi.distanceX=Math.abs(pmi.toX-pmi.fromX);
			pmi.distanceY=Math.abs(pmi.toY-pmi.fromY);
			
		}
	}
	
	private void runPassiveMoveItem(){
		if(Double.isInfinite(movingPrecent) || Double.isNaN(movingPrecent))return;
		
		for(PassiveMoveItem pmi:pmiList){
			FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) pmi.view.getLayoutParams();
			
			if(pmi.fromX>pmi.toX){
				layoutParams.leftMargin=(int) (pmi.fromX-pmi.distanceX*movingPrecent);
			}else{
				layoutParams.leftMargin=(int) (pmi.fromX+pmi.distanceX*movingPrecent);
			}
			
			if(pmi.fromY>pmi.toY){
				layoutParams.topMargin=(int) (pmi.fromY-pmi.distanceY*movingPrecent);
			}else{
				layoutParams.topMargin=(int) (pmi.fromY+pmi.distanceY*movingPrecent);
			}
			pmi.view.setLayoutParams(layoutParams);
			
			pmi.view.invalidate();
		}
	}
	
	private void runActiveMoveItem(){
		if(activeView==null)return;
		
		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) activeView.getLayoutParams();
		
		if(fromX>toX){
			layoutParams.leftMargin=(int) (fromX-distanceX*movingPrecent);
		}else{
			layoutParams.leftMargin=(int) (fromX+distanceX*movingPrecent);
		}
		
		if(fromY>toY){
			layoutParams.topMargin=(int) (fromY-distanceY*movingPrecent);
		}else{
			layoutParams.topMargin=(int) (fromY+distanceY*movingPrecent);
		}
		activeView.setLayoutParams(layoutParams);
		
		activeView.invalidate();
	}
	
	private class PassiveMoveItem{
		public View view;
		public int fromX,fromY;
		public int toX,toY;
		public int distanceX,distanceY;
		
		public PassiveMoveItem(View view,int toX,int toY){
			this.view=view;
			this.toX=toX;
			this.toY=toY;
		}
		
	}
	
	OnMovingListener myOnMovingListener;//移动监听器
	/**
	 * 设置移动监听接口
	 * @param myOnMovingListener 移动监听器
	 */
	public void setOnMovingListener(OnMovingListener myOnMovingListener){
		this.myOnMovingListener=myOnMovingListener;
	}
	public interface OnMovingListener{
		/**
		 * 移动中
		 * @param movingPrecent 移动百分比
		 */
		public void onMoving(double movingPrecent);
	}
	
	class MoveItemTouchListener implements OnTouchListener{
		View view;
		
		public MoveItemTouchListener(final View view,int toX,int toY){
			this.view=view;
			MoveLayout.this.toX=toX;
			MoveLayout.this.toY=toY;
			
			ViewTreeObserver vto = view.getViewTreeObserver();
			vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				boolean hasMeasured=false;
				public boolean onPreDraw() {
					if (hasMeasured == false) {
						FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
						layoutParams.leftMargin=view.getLeft();
						layoutParams.topMargin=view.getTop();
						layoutParams.gravity=-1;//清除所有gravity布局
						view.setLayoutParams(layoutParams);
						
						activeMoveItemInit();
						passiveMoveItemInit();
						
						hasMeasured=true;
					}
					return true;
				}
			});
		}
		
		private void activeMoveItemInit(){
			fromX=view.getLeft();
			fromY=view.getTop();
			
			distance=Math.sqrt(Math.pow(toX-fromX,2)+Math.pow(toY-fromY,2));
			distanceX=Math.abs(toX-fromX);
			distanceY=Math.abs(toY-fromY);
			
			if(fromX==toX){
				mode=Y;
				
			}else if(fromY==toY){
				mode=X;
				
			}else{
				mode=XY;
				
				k=(toY-fromY)/(double)(toX-fromX);
				b=fromY-k*fromX;
				
				kd=0-1/k;
			}
		}
		
		private int _xDelta;  
	    private int _yDelta;  
	    
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			// TODO 自动生成的方法存根
			final int rawX = (int) event.getRawX();
			final int rawY = (int) event.getRawY();
			int action = event.getAction();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				LayoutParams lParams = (LayoutParams) view.getLayoutParams();
				_xDelta = rawX - lParams.leftMargin;
				_yDelta = rawY - lParams.topMargin;
				break;
			case MotionEvent.ACTION_MOVE:
				
				//模式
				if(mode==X){
					int movingDistance=Math.abs(view.getLeft()-fromX);
					if(movingDistance==0){
						movingPrecent=0;
					}else{
						movingPrecent=movingDistance/distance;
						
						if(view.getLeft()<fromX){//负数
							movingPrecent=0-movingPrecent;
						}
						
						if(toX<fromX){//反向
							movingPrecent=0-movingPrecent;
						}
					}
				}else if(mode==Y){
					int movingDistance=Math.abs(view.getTop()-fromY);
					if(movingDistance==0){
						movingPrecent=0;
					}else{
						movingPrecent=movingDistance/distance;
						
						if(view.getTop()<fromY){//负数
							movingPrecent=0-movingPrecent;
						}
						
						if(toY<fromY){//反向
							movingPrecent=0-movingPrecent;
						}
					}
				}else{//XY
					bd=view.getTop()-kd*view.getLeft();
					
					float[][] matrix = { { (float) -k, 1, (float) b }, { (float) -kd, 1, (float) bd } };
					Matrix.simple(2, matrix);
					float[] xy = Matrix.getResult(2, matrix);//投影在直线上的点
					
					double movingDistance=Math.sqrt(Math.pow(xy[0]-fromX,2)+Math.pow(xy[1]-fromY,2));//移动距离

					movingPrecent=movingDistance/distance;
					if(xy[0]<fromX || xy[1]<fromY){//负数
						movingPrecent=0-movingPrecent;
					}
					
					if(toX<fromX && toY<fromY){//反向
						movingPrecent=0-movingPrecent;
					}
				}
				
				
				int left=0;
				int top=0;
				
				if(isStraight){//走直线
					
					if(mode==X){
						left = rawX - _xDelta;
						top = fromY;
					}else if(mode==Y){
						left = fromX;
						top = rawY - _yDelta;
					}else{
						int x=(int) Math.abs(event.getRawX()-rawX);
						int y=(int) Math.abs(event.getRawY()-rawY);
						if(x>y){
							left = rawX - _xDelta;
							top = (int) (k*left+b);
						}else{
							top = rawY - _yDelta;
							left = (int) ((top-b)/k);
						}						
					}
					
				}else{
					left=rawX - _xDelta;
					top=rawY - _yDelta;
				}
				
				if(useMoveArea){//移动区域
					if(left<minX)left=minX;
					if(left>maxX)left=maxX;
					if(top<minY)top=minY;
					if(top>maxY)top=maxY;
					
					if(isStraight && mode==XY){
						int x=(int) Math.abs(event.getRawX()-rawX);
						int y=(int) Math.abs(event.getRawY()-rawY);
						if(x>y){
							left = rawX - _xDelta;
							if(left<minX)left=minX;
							if(left>maxX)left=maxX;
							
							top = (int) (k*left+b);
						}else{
							top = rawY - _yDelta;
							if(top<minY)top=minY;
							if(top>maxY)top=maxY;
							
							left = (int) ((top-b)/k);
						}
					}
					
				}
				
				//主动view移动
				FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) activeView.getLayoutParams();
				layoutParams.leftMargin = left;
		    	layoutParams.topMargin = top;
		    	
		    	//被动view移动
				runPassiveMoveItem();
				
				//移动监听
				if(myOnMovingListener!=null){
					myOnMovingListener.onMoving(movingPrecent);
				}
				
				view.invalidate();
				
				break;
			case MotionEvent.ACTION_UP:
				break;
			case MotionEvent.ACTION_SCROLL:
				break;
			}

			return true;
		}
	}
	
	/**
	 * 获取移动百分比
	 * @return 0.0——0%，1.0——100%，依此类推
	 */
	public double getMovingPrecent(){
		return movingPrecent;
	}
	
	/**
	 * 解方程组
	 */
	static class Matrix {

		public static void simple(int n, float[][] matrix) {
			for (int k = 0; k < n; k++) {
				if (matrix[k][k] == 0) {
					changeRow(n, k, matrix);
				}

				for (int i = 0; i < n; i++) {
					// 记录对角线元素，作为除数
					float temp = matrix[i][k];
					for (int j = 0; j < n + 1; j++) {
						// i<k时,i行已经计算完成
						if (i < k)
							break;
						if (temp == 0)
							continue;
						if (temp != 1) {
							matrix[i][j] /= temp;
						}

						if (i > k)
							matrix[i][j] -= matrix[k][j];
					}
				}
			}

		}

		public static float[] getResult(int n, float[][] matrix) {
			float[] result = new float[n];
			for (int i = n - 1; i >= 0; i--) {
				float temp = matrix[i][n];
				for (int j = n - 1; j >= 0; j--) {
					if (i < j && matrix[i][j] != 0) {
						temp = temp - result[j] * matrix[i][j];
					}
				}
				temp /= matrix[i][i];
				result[i] = temp;

			}

			for (int k = 0; k < result.length; k++) {
				System.out.println("X" + (k + 1) + " = " + result[k]);
			}

			return result;
		}

		// 对角线上元素为０时候和下行交换
		public static void changeRow(int n, int k, float[][] matrix) {
			float[] temp = new float[n + 1];
			// if()
			for (int i = k; i < n; i++) {
				// 已到最后一列,不能继续交换
				if (i + 1 == n && matrix[k][k] == 0) {
					System.out.println("无解或有不唯一解！");
					return;
				}

				for (int j = 0; j < n + 1; j++) {
					temp[j] = matrix[k][j];
					matrix[k][j] = matrix[i + 1][j];
					matrix[i + 1][j] = temp[j];
				}
				if (matrix[k][k] != 0)
					return;

			}
		}
	}
}
