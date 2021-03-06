package umm3601.doorboard;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.http.util.ContextUtil;
import io.javalin.plugin.json.JavalinJson;
import umm3601.JwtProcessor;


public class DoorBoardControllerSpec{

  MockHttpServletRequest mockReq = new MockHttpServletRequest();
  MockHttpServletResponse mockRes = new MockHttpServletResponse();

  @InjectMocks
  private DoorBoardController doorBoardController;

  private ObjectId samsId;

  static MongoClient mongoClient;
  @Spy
  static MongoDatabase db;

  @Mock(name = "jwtProcessor")
  JwtProcessor jwtProcessorMock;

  static ObjectMapper jsonMapper = new ObjectMapper();

  private static final String NEW_USER_SUB = "I, Don Quixote, the Lord of La Mancha!";

  private void useJwtForSam() {
    // Make a fake DecodedJWT for jwtProcessorMock to return.
    // (Sam's sub is "frogs are the best")
    DecodedJWT mockDecodedJWT = Mockito.mock(DecodedJWT.class);
    when(mockDecodedJWT.getSubject()).thenReturn("frogs are the best");
    when(jwtProcessorMock.verifyJwtFromHeader(any()))
      .thenReturn(mockDecodedJWT);
  }

  private void useJwtForNewUser() {
    DecodedJWT mockDecodedJWT = Mockito.mock(DecodedJWT.class);
    when(mockDecodedJWT.getSubject()).thenReturn(NEW_USER_SUB);
    when(jwtProcessorMock.verifyJwtFromHeader(any()))
      .thenReturn(mockDecodedJWT);
  }

  private void useInvalidJwt() {
    when(jwtProcessorMock.verifyJwtFromHeader(any()))
      .thenThrow(new UnauthorizedResponse());
  }

  @BeforeAll
  public static void setupAll() {
    String mongoAddr = System.getenv().getOrDefault("MONGO_ADDR", "localhost");
    mongoClient = MongoClients.create(
      MongoClientSettings.builder()
      .applyToClusterSettings(builder ->
      builder.hosts(Arrays.asList(new ServerAddress(mongoAddr))))
      .build());

    db = mongoClient.getDatabase("test");
  }

  @BeforeEach
  public void setupEach() throws IOException {
    MockitoAnnotations.initMocks(this);

    // Reset our mock request and response objects
    mockReq.resetAll();
    mockRes.resetAll();

    MongoCollection<Document> doorBoardDocuments = db.getCollection("doorBoards");
    doorBoardDocuments.drop();
    List<Document> testDoorBoards = new ArrayList<>();
    testDoorBoards.add(Document.parse(
      "{\n" +
      " name: \"Billy \" ,\n" +
      " building: \"Wild \" ,\n" +
      " officeNumber: \"1234 \" ,\n" +
      " email: \"billythekid@this.that \" ,\n" +
      " sub: \"cattlerustler\" \n" +
      "}"
    ));
    testDoorBoards.add(Document.parse(
      "{\n" +
      " name: \"George Washington \" ,\n" +
      " building: \"White House \" ,\n" +
      " officeNumber: \"1789 \" ,\n" +
      " email: \"revolution@freedom.us.fake \" ,\n" +
      " sub: \"1president\" \n" +
      "}"
    ));
    testDoorBoards.add(Document.parse(
      "{\n" +
      " name: \"Not a cat \" ,\n" +
      " building: \"Building for people\" ,\n" +
      " officeNumber: \"1111 \", \n" +
      " email: \"totallynotacat@eatmice.com \" ,\n" +
      " sub: \"meow\" \n" +
      "}"
    ));

    samsId = new ObjectId();
    BasicDBObject sam = new BasicDBObject("_id", samsId);
    sam = sam.append("name", "Sam")
      .append("building", "HFA")
      .append("officeNumber", "23")
      .append("email", "sam@frogs.com")
      .append("sub", "frogs are the best");

    doorBoardDocuments.insertMany(testDoorBoards);
    doorBoardDocuments.insertOne(Document.parse(sam.toJson()));
  }

  @AfterAll
  public static void teardown(){
    db.drop();
    mongoClient.close();
  }

  @Test
  public void GetAllDoorBoardsWithJwt() {
    Context ctx = ContextUtil.init(mockReq, mockRes, "api/doorBoards");

    useJwtForSam();

    doorBoardController.getDoorBoards(ctx);

    assertEquals(200, mockRes.getStatus());

    String result = ctx.resultString();
    assertEquals(
      db.getCollection("doorBoards").countDocuments(),
      JavalinJson.fromJson(result, DoorBoard[].class).length,
      "Wrong number of entries"
    );
  }

  // DoorBoards aren't private; you should be able to get them with or without
  // a JWT.
  @Test
  public void GetAllDoorBoardsWithoutJwt() {
    Context ctx = ContextUtil.init(mockReq, mockRes, "api/doorBoards");

    useInvalidJwt();

    doorBoardController.getDoorBoards(ctx);

    assertEquals(200, mockRes.getStatus());

    String result = ctx.resultString();
    assertEquals(
      db.getCollection("doorBoards").countDocuments(),
      JavalinJson.fromJson(result, DoorBoard[].class).length,
      "Wrong number of entries"
    );
  }


  @Test
  public void GetDoorBoardWithExistentIdWithJwt() throws IOException {

    String testID = samsId.toHexString();

    useJwtForSam();

    Context ctx = ContextUtil.init(mockReq, mockRes, "api/doorBoards/:id", ImmutableMap.of("id", testID));
    doorBoardController.getDoorBoard(ctx);

    assertEquals(200, mockRes.getStatus());

    String result = ctx.resultString();
    DoorBoard resultDoorBoard = JavalinJson.fromJson(result, DoorBoard.class);

    assertEquals(resultDoorBoard._id, samsId.toHexString());
    assertEquals(resultDoorBoard.name, "Sam");
    assertEquals(resultDoorBoard.email, "sam@frogs.com");
  }

  // Similarly, you can get an individual DoorBoard without a JWT.
  @Test
  public void GetDoorBoardWithExistentIdWithoutJwt() throws IOException {

    String testID = samsId.toHexString();

    useInvalidJwt();

    Context ctx = ContextUtil.init(mockReq, mockRes, "api/doorBoards/:id", ImmutableMap.of("id", testID));
    doorBoardController.getDoorBoard(ctx);

    assertEquals(200, mockRes.getStatus());

    String result = ctx.resultString();
    DoorBoard resultDoorBoard = JavalinJson.fromJson(result, DoorBoard.class);

    assertEquals(resultDoorBoard._id, samsId.toHexString());
    assertEquals(resultDoorBoard.name, "Sam");
    assertEquals(resultDoorBoard.email, "sam@frogs.com");
  }


  @Test
  public void GetDoorBoardWithBadId() throws IOException {

    Context ctx = ContextUtil.init(mockReq, mockRes, "api/doorBoards/:id", ImmutableMap.of("id", "bad"));

    useJwtForSam();

    assertThrows(BadRequestResponse.class, () -> {
      doorBoardController.getDoorBoard(ctx);
    });
  }

  @Test
  public void GetDoorBoardWithNonexistentId() throws IOException {

    Context ctx = ContextUtil.init(mockReq, mockRes, "api/doorBoard/:id", ImmutableMap.of("id", "58af3a600343927e48e87335"));

    useJwtForSam();

    assertThrows(NotFoundResponse.class, () -> {
      doorBoardController.getDoorBoard(ctx);
    });
  }

  @Test
  public void AddDoorBoardWithJwt() throws IOException {

    String testNewDoorBoard = "{\"name\": \"Test Owner\", "
    + "\"building\": \"place\", "
    + "\"officeNumber\": \"0000\", "
    + "\"email\": \"test@example.com\", "
    + "\"sub\": \"" + NEW_USER_SUB + "\" }";

    mockReq.setBodyContent(testNewDoorBoard);
    mockReq.setMethod("POST");

    useJwtForNewUser();

    Context ctx = ContextUtil.init(mockReq, mockRes, "api/doorBoards/new");

    doorBoardController.addNewDoorBoard(ctx);

    assertEquals(201, mockRes.getStatus());

    String result = ctx.resultString();
    String id = jsonMapper.readValue(result, ObjectNode.class).get("id").asText();
    assertNotEquals("", id);

    assertEquals(1, db.getCollection("doorBoards").countDocuments(eq("_id", new ObjectId(id))));

    //verify DoorBoard was added to the database under the correct ID
    Document addedDoorBoard = db.getCollection("doorBoards").find(eq("_id", new ObjectId(id))).first();
    assertNotNull(addedDoorBoard);
    assertEquals("Test Owner", addedDoorBoard.getString("name"));
    assertEquals("place", addedDoorBoard.getString("building"));
    assertEquals("0000", addedDoorBoard.getString("officeNumber"));
    assertEquals("test@example.com", addedDoorBoard.getString("email"));
    assertEquals("test@example.com", addedDoorBoard.getString("email"));
  }

  @Test
  public void YouCantAddADoorBoardNotLoggedIn() throws IOException {

    String testNewDoorBoard = "{\"name\": \"Test Owner\", "
    + "\"building\": \"place\", "
    + "\"officeNumber\": \"0000\", "
    + "\"email\": \"test@example.com\", "
    + "\"sub\": \"" + NEW_USER_SUB + "\" }";

    mockReq.setBodyContent(testNewDoorBoard);
    mockReq.setMethod("POST");

    useInvalidJwt();

    Context ctx = ContextUtil.init(mockReq, mockRes, "api/doorBoards/new");

    assertThrows(UnauthorizedResponse.class, () -> {
      doorBoardController.addNewDoorBoard(ctx);
    });
  }

  @Test
  public void YouCantAddADoorBoardForAnotherUser() throws IOException {

    String testNewDoorBoard = "{\"name\": \"Test Owner\", "
    + "\"building\": \"place\", "
    + "\"officeNumber\": \"0000\", "
    + "\"email\": \"test@example.com\", "
    + "\"sub\": \"" + NEW_USER_SUB + "\" }";

    mockReq.setBodyContent(testNewDoorBoard);
    mockReq.setMethod("POST");

    useJwtForSam();

    Context ctx = ContextUtil.init(mockReq, mockRes, "api/doorBoards/new");

    assertThrows(ForbiddenResponse.class, () -> {
      doorBoardController.addNewDoorBoard(ctx);
    });
  }

  @Test
  public void AddInvalidEmailDoorBoard() throws IOException {
    String testNewDoorBoard = "{\n\t\"name\": \"Test Owner\",\n\t\"building\": \"place\",\n\t\"officeNumber\": \"5432\",\n\t\"email\": \"invalidemail\", sub: \"" + NEW_USER_SUB + "\" }";
    mockReq.setBodyContent(testNewDoorBoard);
    mockReq.setMethod("POST");
    Context ctx = ContextUtil.init(mockReq, mockRes, "api/doorBoards/new");

    assertThrows(BadRequestResponse.class, () -> {
      doorBoardController.addNewDoorBoard(ctx);
    });
  }
}
