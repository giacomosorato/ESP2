#include <jni.h>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <cmath>
#include <iostream>
#include <vector>
#include <android/log.h>

#include "opencv2/features2d/features2d.hpp"
#include "opencv2/contrib/contrib.hpp"
#include <fstream>
#include <string>     // std::string, std::to_string

using namespace cv;
using namespace std;
RNG rng(12345);
#define  LOG_TAG "mixed_sample"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)


//estrae le surf dalle immagini campione e le salva nel file "trainDescriptors.yml"
int saveTrainDescriptors()
{
   // cv::initModule_nonfree();  //importante per sify e surf

    Ptr<FeatureDetector> featureDetector;
    Ptr<DescriptorExtractor> descriptorExtractor;

    featureDetector = FeatureDetector::create( "ORB" );
    descriptorExtractor = DescriptorExtractor::create( "FREAK" );

    vector<Mat> trainImages;
    vector<string> trainImagesNames;
 //readImages( "trainImages.txt", trainImages, trainImagesNames );
    Mat img1 = imread("/sdcard/Pictures/PalmizioImg/gioconda.jpg");
    //Mat img2 = imread("/sdcard/Pictures/PalmizioImg/notte.jpg");
    Mat img3 = imread("/sdcard/Pictures/PalmizioImg/picasso.jpg");
    Mat img4 = imread("/sdcard/Pictures/PalmizioImg/urlo.jpg");
    Mat img5 = imread("/sdcard/Pictures/PalmizioImg/danza.jpg");
    trainImages.push_back(img1);
    //trainImages.push_back(img2);
    trainImages.push_back(img3);
    trainImages.push_back(img4);
    trainImages.push_back(img5);

    vector<vector<KeyPoint> > trainKeypoints;
    cout << endl << "< Extracting keypoints from images..." << endl;
    featureDetector->detect( trainImages, trainKeypoints );

    vector<Mat> trainDescriptors;
    cout << "< Computing descriptors for keypoints..." << endl;
    descriptorExtractor->compute( trainImages, trainKeypoints, trainDescriptors );

    //per debug stampo il numero di descrittori
    int totalTrainDesc = 0;
    for( vector<Mat>::const_iterator tdIter = trainDescriptors.begin(); tdIter != trainDescriptors.end(); tdIter++ )
        totalTrainDesc += tdIter->rows;

    cout  << "Total train descriptors count: " << totalTrainDesc << endl;
    cout << ">" << endl;

    FileStorage fs("/sdcard/Pictures/PalmizioImg/trainDescriptors.yml", FileStorage::WRITE);
    //write(fs, "descriptors_1", tempDescriptors_1);
    fs << "numberOfImages" << (int) trainDescriptors.size();

    for( int i=0; (int) i<trainDescriptors.size(); i++ )
    {
        //per convertire i da int a stringa mi tocca fare tutto sto casin:
        stringstream ss;
        ss << i;
        string nome = ss.str();
        string str = "image_" + nome;
        write(fs, str, Mat(trainDescriptors[i]));

    }

    fs.release();



    return 0;
}

//
Mat findQueryDescriptors(string queryImageName)
{
    Mat img = imread( queryImageName, CV_LOAD_IMAGE_GRAYSCALE);

    Ptr<FeatureDetector> featureDetector;
    Ptr<DescriptorExtractor> descriptorExtractor;

    featureDetector = FeatureDetector::create( "ORB" );
    descriptorExtractor = DescriptorExtractor::create( "FREAK" );

    vector<KeyPoint> queryKeypoints;
    //cout << endl << "< Extracting keypoints from image query..." << endl;
    featureDetector->detect( img, queryKeypoints );

    Mat queryDescriptors;
    //cout << "< Computing descriptors for keypoints..." << endl;
    descriptorExtractor->compute( img, queryKeypoints, queryDescriptors );
    //cout << "Query descriptors count: " << queryDescriptors.rows << endl;

    return queryDescriptors;

}



extern "C"
{
	JNIEXPORT void JNICALL Java_com_example_palmizio_WrapperC_FindRect(JNIEnv*, jobject, jlong src_p, jlong dst_r);

	JNIEXPORT jstring JNICALL Java_com_example_palmizio_WrapperC_FindFreak(JNIEnv* env, jobject, jlong src_p);

	JNIEXPORT void JNICALL Java_com_example_palmizio_WrapperC_Train(JNIEnv*, jobject);

	JNIEXPORT void JNICALL Java_com_example_palmizio_WrapperC_AddImg(JNIEnv*, jobject, jstring path);

	JNIEXPORT void JNICALL Java_com_example_palmizio_WrapperC_AddImg(JNIEnv* env, jobject, jstring path)
	{
		const char* nativeString = env->GetStringUTFChars(path, 0);

		Mat descriptors = findQueryDescriptors(nativeString);

		FileStorage fs2("/sdcard/Pictures/PalmizioImg/trainDescriptors.yml", FileStorage::READ);

		int numOfImages;
		fs2["numberOfImages"] >> numOfImages;

	    vector<Mat> trainDescriptors;
	    Mat tmp;
	    for (int i=0; (int) i<numOfImages; i++)
	    {
	        //per convertire i da int a stringa mi tocca fare tutto sto casin:
	        stringstream ss;
	        ss << i;
	        string nome = ss.str();
	        cout << i <<endl;
	        string str = "image_" + nome;

	        fs2[str] >> tmp;
	        trainDescriptors.push_back( tmp );
	    }
	    //accodo l'ultima mat calcolata:
	    trainDescriptors.push_back( descriptors );

		fs2.release();

		int newIdx = numOfImages;

		//per convertire da int a stringa mi tocca fare tutto sto casin:
		stringstream ss;
		ss << newIdx;
		string nome = ss.str();
		string str = "image_" + nome;

		numOfImages++;

		FileStorage fs("/sdcard/Pictures/PalmizioImg/trainDescriptors.yml", FileStorage::WRITE);
		fs << "numberOfImages" << (int) numOfImages;
		//write(fs, str, descriptors);
		//fs << str << descriptors;
	    for( int i=0; (int) i<trainDescriptors.size(); i++ )
	    {
	        //per convertire i da int a stringa mi tocca fare tutto sto casin:
	        stringstream ss;
	        ss << i;
	        string nome = ss.str();
	        string str = "image_" + nome;
	        write(fs, str, Mat(trainDescriptors[i]));

	    }

		fs.release();

	}



	JNIEXPORT void JNICALL Java_com_example_palmizio_WrapperC_Train(JNIEnv*, jobject)
	{
		saveTrainDescriptors();
	}

	JNIEXPORT jstring JNICALL Java_com_example_palmizio_WrapperC_FindFreak(JNIEnv* env, jobject, jlong src_p)
		{
			Mat& img  = *(Mat*)src_p; 	//l'ingresso lo vuole in gray
			Mat gray;
			cvtColor(img, gray, CV_RGBA2GRAY);
			//Mat& mRgb = *(Mat*)dst_r;

			Ptr<FeatureDetector> featureDetector;
			Ptr<DescriptorExtractor> descriptorExtractor;

			featureDetector = FeatureDetector::create( "ORB" );
			descriptorExtractor = DescriptorExtractor::create( "FREAK" );

			vector<KeyPoint> queryKeypoints;
			featureDetector->detect( gray, queryKeypoints );

			Mat queryDescriptors;

			descriptorExtractor->compute( gray, queryKeypoints, queryDescriptors );
			//cout << "Query descriptors count: " << queryDescriptors.rows << endl;

			for( unsigned int i = 0; i < queryKeypoints.size(); i++ )
			{
				const KeyPoint& kp = queryKeypoints[i];
				circle(img, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
				//cvtColor(mRgb, mGr, CV_RGBA2GRAY); // Assuming RGBA input

			}
			/////////////////////////////MATCHING:

			Ptr<DescriptorMatcher> descriptorMatcher;
			descriptorMatcher = DescriptorMatcher::create( "BruteForce-Hamming" );

			FileStorage fs2("/sdcard/Pictures/PalmizioImg/trainDescriptors.yml", FileStorage::READ);

			int itNr;
			fs2["numberOfImages"] >> itNr;

		    vector<Mat> trainDescriptors;
		    Mat tmp;
		    for (int i=0; (int) i<itNr; i++)
		    {
		        //per convertire i da int a stringa mi tocca fare tutto sto casin:
		        stringstream ss;
		        ss << i;
		        string nome = ss.str();
		        cout << i <<endl;
		        string str = "image_" + nome;

		        fs2[str] >> tmp;
		        trainDescriptors.push_back( tmp );
		    }

		    fs2.release();

		    descriptorMatcher->add( trainDescriptors ); //qui gli passo le features delle immagini training
		    descriptorMatcher->train();     //qui faccio il training

		    vector<DMatch> matches;
		    descriptorMatcher->match( queryDescriptors, matches );  //qui faccio il confronto

		    stringstream output;
		    output << "numero di match: " << matches.size() << " :: " ; //<< DMatch(matches[0]).imgIdx << DMatch(matches[1]).imgIdx << DMatch(matches[2]).imgIdx;

		    //quadro contiene # di match associati al quadro i-esimo del dataset
		    //distance contiene la distanza media dalle features dell' i-esimo quadro del dataset
		    int quadro[itNr];
		    float distance[itNr];
		    for (int i = 0; i < itNr; i++)
		    {
		    	quadro[i] = 0;
		    	distance[i] = 0;
		    }


		    for (int i = 0; i < matches.size(); i++)
		    {
		    	int index = DMatch(matches[i]).imgIdx;
		    	quadro[index]++;
		    	distance[index] += DMatch(matches[i]).distance;
		    }

		    for (int i = 0; i < itNr; i++)
		    {	//gestisco divisione per 0:
		    	if (quadro[i] > 0) {distance[i] = distance[i]/quadro[i];}
		    	else distance[i] = 2000;
		    }

		    for (int i = 0; i < itNr; i++)
		    {
		    	output << "quadro " << i << ": " << quadro[i] << " | " ;
		    }

		    output << endl;

		    //trovo la distanza minima:
		    float min = 3000;
		    int idxMin = 0;
		    for (int i = 0; i < itNr; i++)
		    {
		    	if (distance[i] < min)
		    	{
		    		min = distance[i];
		    		idxMin = i;
		    	}
		    	output << "dist. " << i << ": " << distance[i] << " | " ;
		    }
/*
		    //controllo che la media minima sia sufficentemente minore delle altre, se no nono indeciso
		    for (int i = 0; i < itNr; i++)
		    {
		    	if ((distance[i] - min) < 5.5 ) { idxMin = -2;}
		    }
*/
		    //se ho troppi pochi match torno -1 (quadro non riconosciuto)
		    if (quadro[idxMin] < 10) {idxMin = -1;}
		    //controllo che la distanza minima non sia troppo alta:
		    if (min > 85) {idxMin = -1;}

		    output << endl << "quadro suggerito =>" << idxMin;










/*
		    int numZero=0;
		    float m0d=0;
		    int numUno=0;
		    float m1d=0;
		    int numDue=0;
		    float m2d=0;
		    int numTre=0;
		    float m3d=0;
		    int numQuattro=0;
		    float m4d=0;
		    for(int i=0; (int) i< matches.size(); i++)
		        {
					//if (DMatch(matches[i]).distance < 90)
					//{
						switch ((int)  DMatch(matches[i]).imgIdx)
						{
								case 0:
									numZero++;
									m0d=m0d+DMatch(matches[i]).distance;
									break;
								case 1:
									numUno++;
									m1d=m1d+DMatch(matches[i]).distance;
									break;
								case 2:
									numDue++;
									m2d=m2d+DMatch(matches[i]).distance;
									break;
								case 3:
									numTre++;
									m3d=m3d+DMatch(matches[i]).distance;
									break;
								case 4:
									numQuattro++;
									m4d=m4d+DMatch(matches[i]).distance;
									break;
						}

					   //int y = (int)  DMatch(matches[i]).imgIdx;
					   //output << y ;
					//}
		        }
		    // calcolo le medie
		    m0d=m0d/numZero;
		    m1d=m1d/numUno;
		    m2d=m2d/numDue;
		    m3d=m3d/numTre;
		    m4d=m4d/numQuattro;
	*/

		    //////////////////////////////////////////////////////////

		    //////////////////////////////////////////////////////////


		//    output << "zero: "<< numZero << "; " << "uno: "<< numUno << "; " << "due: "<< numDue << "; " << "tre: "<< numTre << "; " << "quattro: "<<numQuattro << "; " << endl;
		//    output <<  "Medie distanze - zero: "<< m0d << "; " << "uno: "<< m1d << "; " << "due: "<< m2d << "; " << "tre: "<< m3d << "; " << "quattro: "<< m4d ;
		//    output << "quadro suggerito: ";


		    /// classificazione: la distanza media è piu importante del numero di match.
		    //  delle foto nel set training, vince il quadro che ha distanza minima SEMPRE.
		    //  se ho una distanza minima superiore a 95 o ci sono distanze minime troppo vicine (es 80 e 85),
		    //  dico che non sono riuscito a riconoscere il quadro

		    string strOut = output.str();
		    const char * c = strOut.c_str();

		    return env->NewStringUTF(c);


		}


    // argomenti metodo: input ed output
	JNIEXPORT void JNICALL Java_com_example_palmizio_WrapperC_FindRect(JNIEnv*, jobject, jlong src_p, jlong dst_r)
	{
		Mat& src  = *(Mat*)src_p;
		Mat& dst = *(Mat*)dst_r;

		int end=false;
		const int DETECTION_WIDTH = 120;

		Size s = src.size();

		// Convert to grayscale
		cv::Mat gray;
		cvtColor(src, gray, CV_BGR2GRAY);

		// Convert to binary image using Canny
		cv::Mat bw;
		Canny(gray, bw, 0, 50, 5);

		// Find contours
		vector< vector<Point> > contours;
		findContours(bw.clone(), contours, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);

		// Rects extraction
		vector< vector<Point> > contours_poly (contours.size());
		vector< vector<Rect> > good_rect;
		vector<Rect> bound_rect (contours.size());

		int good_rect_index=0;

		LOGI("sto per eseguire approxPolyDP");
		for (int i = 0; i < contours.size(); i++)
		{
			vector<Point> temp;
			approxPolyDP( Mat(contours[i]), temp , 3, true );
			contours_poly[i]=temp;

			// extraciont only poligon=rect
			bound_rect[i] = boundingRect( Mat(contours_poly[i]) );
			//-------------
			Rect temp_rect=bound_rect[i];
			if (temp_rect.area() > (s.area()*0.30) )
			{
				// creo un vettore di vettori di rect perch� poi riempir� ogni vettore con i rect compatibili
				// con il primo elemento
				std::vector<cv::Rect> vector;
				vector.push_back(temp_rect);
				good_rect.push_back(vector); // estrapolo tutti i rettangoli buoni
				good_rect_index++;
			}
		}

		if (good_rect.size()==0 || good_rect.size()==1)
			// non si sono trovati rettangoli compatibili, forse
			// l'immagine � una parte interna di quadro
		{
			dst=src;
			end=true;
		}

		if (end==false)
		{
			LOGI("ingresso in end=false");
			// adesso di questi rettangoli buoni devo prendere quelli che effettivamente costituiscono il mio quadro
			for (int j = 0; j < good_rect_index; j++)
			{
				Rect confront = good_rect[j][0];
				int x_center = confront.x+confront.width/2;
				int y_center = confront.y+confront.height/2;

				for (int i = 0; i < good_rect_index; i++)
				{
					vector<Rect> temp_vector;
					Rect temp_confront=good_rect[i][0];
					if (j==i)
						continue;
					else if (x_center > temp_confront.x + temp_confront.width/2 - temp_confront.width*0.05)
						if (x_center < temp_confront.x + temp_confront.width/2+temp_confront.width*0.05)
							if (y_center > temp_confront.y+temp_confront.height/2-temp_confront.height*0.05)
								if (y_center < temp_confront.y+temp_confront.height/2+temp_confront.height*0.05)
									{
										//Mat(good_rect[i]).push_back(confront);
									    Mat(good_rect[i]).push_back(confront);
									}
				}// sto codicione che andr� semplificato praticamente controlla il centro dei rettangoli e definisce
				 // due rettangoli come buoni se hanno lo stesso centro (con un minimo di errore accettabile)
				 // infatti ho deciso che per me un quadro � una successione di rettangoli con lo stesso centro
				 // (cornice interna + cornice esterna + inizio tela = 3 rettangoli con lo stesso centro),
				 // pi� o meno funziona sempre, a meno di cornici strane.

			}
			LOGI("fine del codicione");
			// Ho un vettore di vettori contenente tutti gli accoppiamenti, prendo l'accoppiamento che
			// contiene i rettangoli pi� grandi e lo chiamo quadro
			vector<Rect> quadro;
			vector<Rect> quadro_iterator;
			for( int i = 0; i< good_rect_index; i++ ) // iterate through each contour.
			{
				quadro_iterator=good_rect[i];
				if (i==0)
					quadro=quadro_iterator;
				else if (quadro.size() < quadro_iterator.size())
					{
					quadro=quadro_iterator;
					}
			}

			LOGI("fine ricerca accoppiamento migliore");
			// prendo il rettangolo pi� grande di quadro e taglio l'esterno
			int size = quadro.size();

			// Max rect extraction from the quadro
			int largest_contour_index=0;
			Rect big_rect; // The variable to store max rect


			for( int i = 0; i < quadro.size(); i++ ) // iterate through each contour.
			{
				Rect quadro_temp=quadro[i];
				if (i==0)
					big_rect=quadro_temp;
				else if (big_rect.area() < quadro_temp.area())
					{
						big_rect=quadro_temp;
						largest_contour_index=i;
					}
			}

			LOGI("fine ricerca quadro piu grande");
			// cut the image to have only the quadro
			src(big_rect).copyTo(dst);

		}

		// Possibly shrink the image, to run much faster.
		//Mat smallDst;
		//float scale = dst.cols / (float) DETECTION_WIDTH;
		//if (dst.cols > DETECTION_WIDTH) {
			// Shrink the image while keeping the same aspect ratio.
		//	int scaledHeight = cvRound(dst.rows / scale);
			//resize(dst, smallDst, Size(480, 640));
	//	}
		//else {
			// Access the input directly since it is already small.
		//	smallDst = dst;
		//}

		//dst=smallDst;
	}
}
