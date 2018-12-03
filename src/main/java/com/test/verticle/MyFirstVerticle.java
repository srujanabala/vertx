package com.test.verticle;

import java.util.ArrayList;
import java.util.List;

import com.test.model.Student;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MyFirstVerticle extends AbstractVerticle {
	
	private List<Student> students = new ArrayList<>();
	
	//private CassandraSession cassandraSession;
	
	@Override
	public void init(Vertx vertx, io.vertx.core.Context context) {
		super.init(vertx, context);
        //cassandraSession = new DefaultCassandraSession( new Cluster.Builder(), new JsonCassandraConfigurator(vertx), vertx);
	}
	
    @Override
    public void start(Future<Void> fut) {    	
    	/*cassandraSession.onReady( (v) -> {
            System.out.printf( "==> CASSANDRA SESSION INITIALIZED\n\t[%b]\n", cassandraSession.initialized() );
            fut.complete();
        });*/
    	
    	createSomeData();

        // Create a router object.
        Router router = Router.router(vertx);

        // Bind "/" to our hello message.
        router.route("/").handler(routingContext -> {
          HttpServerResponse response = routingContext.response();
          response
              .putHeader("content-type", "text/html")
              .end("<h1>Hello from my first Vert.x 3 application</h1>");
        });
        
        //To access the request body information
        router.route().handler(BodyHandler.create());

       // router.route("/assets/*").handler(StaticHandler.create("assets"));

        router.get("/api/get1").handler(this::getAll);
        router.get("/api/students").handler(this::getAll);
        //router.route("/api/*").handler(BodyHandler.create());
        router.post("/api/students").handler(this::addOne);
        router.get("/api/students/:id").handler(this::getOne);
        router.put("/api/students/:id").handler(this::updateOne);
        router.delete("/api/students/:id").handler(this::deleteOne);


        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
            .createHttpServer()
            .requestHandler(router::accept)
            .listen(
                // Retrieve the port from the configuration,
                // default to 8080.
                config().getInteger("http.port", 8080),
                result -> {
                  if (result.succeeded()) {
                    fut.complete();
                  } else {
                    fut.fail(result.cause());
                  }
                }
            );
    }
    
    private void createSomeData() {    	
    	students.add(new Student(101, "name1"));
    	students.add(new Student(102, "name2"));
	}

	private void getAll(RoutingContext routingContext) {
        // Write the HTTP response
        // The response is in JSON using the utf-8 encoding
        // We returns the list of bottles
        routingContext.response()
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(students));
      }
	
	private void addOne(RoutingContext routingContext) {
	    final Student student = Json.decodeValue(routingContext.getBodyAsString(),
	        Student.class);
	    System.out.println(student);
	    students.add(student);
	    routingContext.response()
	        .setStatusCode(201)
	        .putHeader("content-type", "application/json; charset=utf-8")
	        .end(Json.encodePrettily(student));
	  }
	
	  private void getOne(RoutingContext routingContext) {
		    final String id = routingContext.request().getParam("id");
		    if (id == null) {
		      routingContext.response().setStatusCode(400).end();
		    } else {
		      final Integer idAsInteger = Integer.valueOf(id);
		      Student student = students.stream()
		      	.filter(s -> s.getId()==idAsInteger)
		      	.findFirst().orElse(null);
		      if (student == null) {
		        routingContext.response().setStatusCode(404).end();
		      } else {
		        routingContext.response()
		            .putHeader("content-type", "application/json; charset=utf-8")
		            .end(Json.encodePrettily(student));
		      }
		    }
		  }

		  private void updateOne(RoutingContext routingContext) {
		    final String id = routingContext.request().getParam("id");
		    JsonObject json = routingContext.getBodyAsJson();
		    if (id == null || json == null) {
		      System.out.println(id);
		      System.out.println(json);
		      routingContext.response().setStatusCode(400).end();
		    } else {
		      final Integer idAsInteger = Integer.valueOf(id);
		      Student student = students.stream()
				      	.filter(s -> s.getId()==idAsInteger)
				      	.findFirst().orElse(null);
		      if (student == null) {
		        routingContext.response().setStatusCode(404).end();
		      } else {
		        student.setName(json.getString("name"));
		        routingContext.response()
		            .putHeader("content-type", "application/json; charset=utf-8")
		            .end(Json.encodePrettily(student));
		      }
		    }
		  }

		  private void deleteOne(RoutingContext routingContext) {
		    String id = routingContext.request().getParam("id");
		    if (id == null) {
		      routingContext.response().setStatusCode(400).end();
		    } else {
		      Integer idAsInteger = Integer.valueOf(id);
		      students.remove(students.get(idAsInteger));
		    }
		    routingContext.response().setStatusCode(204).end();
		  }

}