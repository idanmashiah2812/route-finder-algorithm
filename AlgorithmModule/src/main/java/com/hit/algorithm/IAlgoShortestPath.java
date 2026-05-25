package com.hit.algorithm;

import java.util.List;
import java.util.Map;

public interface IAlgoShortestPath {
    List<String> findShortestPath(Map<String, Map<String, Integer>> graph, String source, String destination);
}
