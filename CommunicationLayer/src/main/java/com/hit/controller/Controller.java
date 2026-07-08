package com.hit.controller;

import com.hit.server.Request;
import com.hit.server.Response;

public interface Controller {

    Response<?> handle(Request<?> request);
}
