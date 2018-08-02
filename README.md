LastFM top tracks recommender
=====================================
Program to calculate top tracks among the longest user sessions in http://www.dtic.upf.edu/~ocelma/MusicRecommendationDataset/lastfm-1K.html

##Dependencies
1. Download dataset from http://www.dtic.upf.edu/~ocelma/MusicRecommendationDataset/lastfm-1K.html
2. sbt, java 8 should be installed

##Run locally

###Run
1. Assembly project by running `sbt assembly` in the project root
2. Open `./run.sh` and edit attributes if required
```
SESSIONS_NUMBER=50
TRACKS_NUMBER=10
SESSION_TIMEOUT_MILLIS=1200000
INFER_TRACK_ID="false"
```
3. Execute `./run.sh <path_to>/lastfm-dataset-1K/userid-timestamp-artid-artname-traid-traname.tsv`.
Execution takes on my laptop around 5 minutes.
4. See results in `./output/part-*` file.
 
###Results

With filtered records where trackId is not defined: `INFER_TRACK_ID="false"`
```
count,musicbrainz-track-id,track-name,musicbrainz-artist-id,artist-name
1205,60f0bfa4-8da9-4840-b5fe-23c1fc470f34,Jolene,fa7b9055-3703-473a-8a09-adf2fe031a24,Cake
811,87129015-6dc5-4cfc-b71f-5e908fb01f6e,Pinocchio Story (Freestyle Live From Singapore),164f0d73-1234-4e2c-8743-d77bf2191051,Kanye West
811,cc086205-4e55-4740-a2ce-238d4353b2be,Welcome To Heartbreak (Feat. Kid Cudi),164f0d73-1234-4e2c-8743-d77bf2191051,Kanye West
815,986077d0-162b-43b3-b8ff-514cfd774eba,Heartless,164f0d73-1234-4e2c-8743-d77bf2191051,Kanye West
811,20327ab0-f6c6-4401-b5c3-f7c0fc0d2bd0,Amazing (Feat. Young Jeezy),164f0d73-1234-4e2c-8743-d77bf2191051,Kanye West
810,4c498872-70d2-4b01-8115-6224176a7667,Coldest Winter,164f0d73-1234-4e2c-8743-d77bf2191051,Kanye West
816,82558949-cd98-4c58-af35-3f1a9430d52e,See You In My Nightmares,164f0d73-1234-4e2c-8743-d77bf2191051,Kanye West
813,153d8ca0-dc23-4548-b64f-2c49db7f30db,Love Lockdown,164f0d73-1234-4e2c-8743-d77bf2191051,Kanye West
816,03bd74d1-e7f9-471f-a2b0-f18fd35a4a68,Say You Will,164f0d73-1234-4e2c-8743-d77bf2191051,Kanye West
868,db4c9220-df76-4b42-b6f5-8bf52cc80f77,Heartbeats,bf710b71-48e5-4e15-9bd6-96debb2e4e98,The Knife
```

With inferred id `INFER_TRACK_ID="true"`
```
count,musicbrainz-track-id,track-name,musicbrainz-artist-id,artist-name
1214,60f0bfa4-8da9-4840-b5fe-23c1fc470f34,Jolene,fa7b9055-3703-473a-8a09-adf2fe031a24,Cake
634,Bonus Track-95e1ead9-4d31-4808-a7ac-32c3614c116b-The Killers,Bonus Track,95e1ead9-4d31-4808-a7ac-32c3614c116b,The Killers
613,55956d3e-c5d4-4332-a377-bf77c463656e,Beast Of Burden,b071f9fa-14b0-4217-8e97-eb41da73f598,The Rolling Stones
646,45c773c3-c8f0-41a1-b5a5-c241a3baebfa,St. Ides Heaven,03ad1736-b7c9-412a-b442-82536d63a5c4,Elliott Smith
726,c2b14074-15d6-40cf-a27d-958ab7a8ee2d,How Long Will It Take,5e1a0f80-6200-41fc-9698-daf842fecdd1,Jeff Buckley & Gary Lucas
604,9ad11ca6-c5b9-4a26-9b3b-3943d91eac01,The Swing,3604c99d-c146-4276-aa0c-9376d333aeb8,Everclear
536,82558949-cd98-4c58-af35-3f1a9430d52e,See You In My Nightmares,164f0d73-1234-4e2c-8743-d77bf2191051,Kanye West
868,db4c9220-df76-4b42-b6f5-8bf52cc80f77,Heartbeats,bf710b71-48e5-4e15-9bd6-96debb2e4e98,The Knife
659,91951530-d978-4648-95b1-08b1f49ffba5,Anthems For A Seventeen Year Old Girl,2eada8f8-056a-4093-bbc2-004909ce743b,Broken Social Scene
617,b649b4ba-4912-4add-8bb1-c7f705deceeb,Starin' Through My Rear View,382f1005-e9ab-4684-afd4-0bdae4ee37f2,2Pac
```

##Deploy on spark cluster

1. Package. Execute the following commands in project root:
```
sbt
> project job
> package
```
Jar path `./job/target/scala-2.11/job_2.11-0.1.jar`

2. Submit application. Copy jar to Spark node.
Run on a Spark standalone cluster in cluster deploy mode with supervise:
```
./bin/spark-submit \
  --class com.appsflyer.lastfm.job \
  --master spark://<host>:<port> \
  --deploy-mode cluster \
  --supervise \
  --executor-memory 2G \
  --total-executor-cores 10 \
  /path/to/job_2.11-0.1.jar \
  /path/to/userid-timestamp-artid-artname-traid-traname.tsv \
  /path/to/empty/output/dir \
  50 \
  10 \
  1200000 \
  "false"
```
