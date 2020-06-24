package com.hutsko;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.nodes.Document;

public interface UserParserApi {

    JsonNode getUsers();

    JsonNode getUserById(int id);

}
