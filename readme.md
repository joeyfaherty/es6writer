- docker-compose up -d
- nc -lk 5555 (terminal)
- Run the main class from your favourite IDE (intellij)

- paste in terminal a customer csv in the format: *id,name,age*
- eg: *1,John,17*

- Go to [http://localhost:9200/index/type/_search?pretty=true&q=*:*](http://localhost:9200/index/type/_search?pretty=true&q=*:*) to see your index get updated in real time.