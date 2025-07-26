#!/bin/bash

cd /mnt/Sector16/newstime_k/app/api/ || exit
source env/bin/activate

uvicorn main:app --reload &

sleep 2

ngrok http 8000 &


wait
