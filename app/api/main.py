import requests
from json import load
from fastapi import FastAPI
from fastapi.responses import JSONResponse

def load_tags():
    with open("./data/tags.json") as tags_file:
        data = load(tags_file)
        tags_file.close()

    return data

tags = load_tags()
req_keys = ["article_id", "link", "creator", "description", "pubDate", "image_url", "source_name"]

app = FastAPI()

@app.get("/prefs")
def get_prefs():
    return tags


@app.get("/latest")
def get_latest_news():
    endpoint = "https://newsdata.io/api/1/latest?apikey=pub_94e0bc504a274c75aeaff2adb3badbfc"
    res = requests.get(endpoint)
    if res.status_code != 200:
        return JSONResponse(content={"status": str(res.status_code), "articles": None})
    elif res.status_code == 200:
        data = res.json()
        trim_data = data["results"][:10]
        return JSONResponse(content={"status": res.status_code, "results": preprocess(trim_data)})
    
def preprocess(article_list: list):
    opt_articles = []

    for article in article_list:
        opt_article = {}
        for key in req_keys:
            opt_article[key] = article[key]
        opt_articles.append(opt_article)

    return opt_articles

