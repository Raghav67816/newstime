#!/bin/bash

cd /mnt/Sector16/newstime/api/ || exit
source env/bin/activate

uvicorn main:app --reload &

sleep 2

ngrok http 8000 &


wait
