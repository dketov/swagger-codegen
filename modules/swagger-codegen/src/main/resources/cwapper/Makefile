help:
	@echo make help		- This help
	@echo make build	- Build sources
	@echo make run		- Run service

build:  api

run:	api
	@echo "Point your browser to 'http://localhost:8888/swagger-ui/?url=/api.json'"
	./api -c config.js

api:	
	mkdir -p __build__ && cd __build__ && cmake .. && make
