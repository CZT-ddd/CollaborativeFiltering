import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/*
 * �����û���Эͬ�����Ƽ��㷨
 * �����û��������Եķ���ѡ�ã�������������������
 * ���룺UserID  ��     ItemID
 * ���1��Ԥ������ֵ
 * ���2��RMSE���Ƽ�������
 * */
class UserBaseCF{
	
	public static final int USERSIZE=943;
	public static final int ITEMSIZE=1682;
	public static final int UN=10;//ĳһuser������ھ���
	//public static final int IN=10;//ĳһitem������ھ���
	
	public int [] num=new int[USERSIZE+1];//ÿ���û�Ϊ�������˷�
	public double[] average=new double[USERSIZE+1];//ÿ��user��ƽ�����
	public double[][] rate=new double[USERSIZE+1][ITEMSIZE+1];//���־���
	public double[][] DealedOfRate=new double[USERSIZE+1][ITEMSIZE+1];//���ϡ�����⴦�������־���
	
	Neighbor[][] NofUser =new Neighbor[USERSIZE+1][UN+1];//ÿ���û��������UN���ھ�
	
	List<Double> x=new LinkedList<Double>();//LinkedList���ն�������˳��洢
	List<Double> y=new LinkedList<Double>();
	public static void main(String args[]) throws Exception{
		
		UserBaseCF cf=new UserBaseCF();
		if(cf.readFile("bin/ml-data_0/u1.base")){
			System.out.println("��ȴ������ڷ���");
			cf.getAvr();//�õ�average[]
			cf.dealRate();//�õ�DealedOfRate
			
			cf.getNofUser();//�õ�NofUser
			/* test
			System.out.println(cf.rate[1][11]);
			System.out.println(cf.DealedOfRate[1][11]);
			System.out.println(cf.num[1]);
			System.out.println(cf.average[1]);
			System.out.println(cf.rate[1][10]);
			System.out.println(cf.DealedOfRate[1][10]);
		*/
			for(int i=1;i<=UN;i++){
				System.out.println(cf.NofUser[1][i].getID()+":"+cf.NofUser[1][i].getValue());
			}
			
			
			//����
			//���ļ�
			File inputFile=new File("bin/ml-data_0/u1.test");
			BufferedReader reader=null;
	        if(!inputFile.exists()||inputFile.isDirectory())
					throw new FileNotFoundException();
	        reader=new BufferedReader(new FileReader(inputFile));
	        
	        //д�ļ�
	        File outputFile=new File("bin/testResult.txt");
	        FileWriter writer=null;
	        if(!outputFile.exists())
	        	if(!outputFile.createNewFile())
	        		System.out.println("����ļ�����ʧ��");
	        writer=new FileWriter(outputFile);
	        String title ="UserID"+"\t"+"ItemID"+"\t"+"OriginalRate"+"\t"+"PredictRate"+"\r\n";
	        writer.write(title);
	        writer.flush();
	        String[] part=new String[3];
	        String tmpToRead="";
	        String tmpToWrite="";
	        while((tmpToRead=reader.readLine())!=null){
	        	part=tmpToRead.split("\t");
	        	int userID=Integer.parseInt(part[0]);
	        	int itemID=Integer.parseInt(part[1]);
	        	double originalRate=Double.parseDouble(part[2]);
	        	double predictRate=cf.predict(userID, itemID);
	        	cf.x.add(originalRate);
	        	cf.y.add(predictRate);
	        	tmpToWrite=userID+"\t"+itemID+"\t"+originalRate+"\t"+predictRate+"\r\n";
	        	writer.write(tmpToWrite);
	        	writer.flush();
	        }
			System.out.println("������ɣ���򿪹���Ŀ¼��bin�ļ����е�testResult.txt");
			System.out.println("����RMSE�������Ϊ"+cf.analyse(cf.x, cf.y));
			
		}
		else 			
			System.out.println("ʧ��");
		
	}
	
	//Chapter1:׼������
		//1-1:��ȡ�ļ����ݣ��õ����־���     1:��ȡ�ɹ�       -1����ȡʧ��
	public boolean readFile(String filePath){
		File inputFile=new File(filePath);
		BufferedReader reader=null;
        try {
			reader=new BufferedReader(new FileReader(inputFile));
		} catch (FileNotFoundException e) {
			System.out.println("�ļ�������"+e.getMessage());
			return false;
		}
		
        String sentence="";
        String[] part=new String[3];
        try {
			while((sentence=reader.readLine())!=null){
				part=sentence.split("\t");
				int userID=Integer.parseInt(part[0]);
				int itemID=Integer.parseInt(part[1]);
				double Rate=Double.parseDouble(part[2]);
				//�������
				rate[userID][itemID]=Rate;
			}
		} catch (NumberFormatException|IOException e) {
			System.out.println("���ļ���������"+e.getMessage());
			return false;
		}
        return true;	
	}
		//1-2����ÿ���û���ƽ����
	public void getLen(){//����ÿ���û�Ϊ������Ӱ���
		for(int i=1;i<=USERSIZE;i++){
			int n=0;
			for(int j=1;j<=ITEMSIZE;j++){
				if(rate[i][j]!=0)
					n++;
			}
			num[i]=n;
		}
	
	}
	public void getAvr(){
		getLen();
		int i,j;
		for(i=1;i<=USERSIZE;i++){
			double sum=0.0;
			for(j=1;j<rate[i].length;j++){//ÿ��length����ITEMSIZE=1682
				sum+=rate[i][j];
			}
			average[i]=sum/num[i];
		}
	}
		//1-3�������־����ϡ�����⣨��Ҫ���������
		//�ص㴦���user��û�б����ֵ�item����򼸷�
		//��ʱ��1-2�м������ƽ����	
	public void dealRate(){
		int  i,j;
		for(i=1;i<=USERSIZE;i++){
			for(j=1;j<=ITEMSIZE;j++){
				if(rate[i][j]==0)
					DealedOfRate[i][j]=average[i];
				else
					DealedOfRate[i][j]=rate[i][j];
			}
		}
	}
	//Chapter2�����࣬�Һ�ĳһ�û�����ͬϲ�õ�һ���û�
		//2-1��:Pearson�������������ƶ�
	public double Sum(double[] arr){
		double total=(double)0.0;
		for(double ele:arr)
			total+=ele;
		return total;
	}
	public double Mutipl(double[] arr1,double[] arr2,int len){
		double total=(double)0.0;
		for(int i=0;i<len;i++)
			total+=arr1[i]*arr2[i];
		return total;
	}
	public double Pearson(double[] x,double[] y){
		int lenx=x.length;
		int leny=y.length;
		int len=lenx;//С�ݴ�
		if(lenx<leny) len=lenx;
		else len=leny;	
		double sumX=Sum(x);
		double sumY=Sum(y);
		double sumXX=Mutipl(x,x,len);
		double sumYY=Mutipl(y,y,len);
		double sumXY=Mutipl(x,y,len);
		double upside=sumXY-sumX*sumY/len;
		//double downside=(double) Math.sqrt((sumXX-(Math.pow(sumX, 2))/len)*(sumYY-(Math.pow(sumY, 2))/len));
		double downside=(double) Math.sqrt((sumXX-Math.pow(sumX, 2)/len)*(sumYY-Math.pow(sumY, 2)/len));
		
		//System.out.println(len+" "+sumX+" "+sumY+" "+sumXX+" "+sumYY+" "+sumXY);
		return upside/downside;
	}
	
		//2-2��Pearson�㷨������user�Ľ����ϣ���NofUser����
	public void getNofUser(){
		int  id,userID;
		for(userID=1;userID<=USERSIZE;userID++){
			Set<Neighbor> neighborList=new TreeSet();//�Ὣѹ���Neighbor�ź�����
			Neighbor[] tmpNeighbor=new Neighbor[USERSIZE+1];
			for(id=1;id<=USERSIZE;id++){
				if(id!=userID){
					double sim=Pearson(DealedOfRate[userID],DealedOfRate[id]);
					tmpNeighbor[id]=new Neighbor(id,sim);
					neighborList.add(tmpNeighbor[id]);
				}
			}
			
			int k=1;
			Iterator it=neighborList.iterator();
			while(k<=UN&&it.hasNext()){
				Neighbor tmp=(Neighbor) it.next();
				NofUser[userID][k]=tmp;
				k++;
			}
		}
	}
	
	//Chapter3:��������ھӸ���Ԥ������
	public double predict(int userID, int itemID){//�����userIDΪ�û����룬��1��Ϊ�����±꣡
		double sum1=0;
	    double sum2=0;
	    for(int i=1;i<=UN;i++){//�������UN���ھӽ��д���
	        int neighborID=NofUser[userID][i].getID();
	        double neib_sim=NofUser[userID][i].getValue();
	        sum1+=neib_sim*(DealedOfRate[neighborID][itemID]-average[neighborID]);
	        sum2+=Math.abs(neib_sim);
	    }
	    return average[userID]+sum1/sum2;
	}
	
	//Chapter4:����
	//��u1.test��userID��itemIDΪ���룬�����������ٸ���һ���֣���u1.test�н��бȽ�
	//���ֲ�������main���������ã�����ʵ�־����ʽRMSE
	//���ǹ۲�ֵ����ֵƫ���ƽ���� �� �۲����n��ֵ��ƽ����
	public double RMSE(double[] x, double[] y){
		double rmse=0;
		int lenx=x.length;
		int leny=y.length;
		int len=lenx;//С�ݴ�
		if(lenx<leny) len=lenx;
		else len=leny;
		
		double diffSum=0;
		double diffMutipl;
		for(int i=0;i<len;i++){
			diffMutipl=Math.pow((x[i]-y[i]), 2);
			diffSum+=diffMutipl;
		}
		rmse=Math.sqrt(diffSum/len);
		System.out.println(len);
		//System.out.println(diff);
		return rmse;
	}
	public double analyse(List<Double>x,List<Double>y){
		int lenx=x.size();
		int leny=y.size();
		int len=lenx;//С�ݴ�
		if(lenx<leny) len=lenx;
		else len=leny;
		//System.out.println(len);
		double[] tmpX=new double[len];
		double[] tmpY=new double[len];
		for(int i=0;i<len;i++){
			tmpX[i]=x.get(i);
			tmpY[i]=y.get(i);
		}
		return RMSE(tmpX,tmpY);
		//System.out.println(tmpY[1]);
	}
}