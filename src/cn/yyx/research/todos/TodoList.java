package cn.yyx.research.todos;

public class TodoList {
	
	// Solved. source methods should be as one, not distributed in different element lists.
	// Solved. direct assignment should give a node to redirect to other list. Just add a Self connection to other list.
	// Solved. method return should be modeled as a distinct element, add Self connection to the method.
	// Solved. Construction method need to be reconsidered method and its class should be a map.
	// Solved. for common instructions, IJavaElement Self connection sill needs to be added.
	
	// Solved. remember to add virtual branch to every node in only one branch�� such as if(){} without else branch.
	// Solved. lambda expressions are needed to be treated as IMethod.
	// Solved. remember to check whether lambda expression method implementations could be searched.
	// Solved. method declaration remember to check parameter list, add null if.
	
	// Solved. variable count and two/three further steps' environment are no longer needed.
	// Solved. SkipSelfTask should add the information of IJavaElement if there are no source connections.
	// TODO wrong when handling invoking constructors in full traces.
	// Solved. do not traverse from root methods (exclude constructors) which contains no statements.
	
	// TODO how to recognize the global relationship, eclipse jdt offers? 
	// TODO how to recognize relationships including code snippet not on the execution path from main root?.
	
}
