/**
 * 
 */


window.onload = function() {
    var fileInput = document.getElementById('fileInput');
    var fileDisplayArea = document.getElementById('fileDisplayArea');
    var documentscore = document.getElementById('documentscore');

    fileInput.addEventListener('change', function(e) {
        var file = fileInput.files[0];
        var textType = /text.*/;

        if (file.type.match(textType)) {
            var reader = new FileReader();

            reader.onload = function(e) {
              //  fileDisplayArea.innerText = reader.result;

                send(reader.result);
            }

            reader.readAsText(file); 
        } else {
            fileDisplayArea.innerText = "File not supported!"
        }
    });
}


function send(text) {
    var inputJSON = '{"text": "' + text + '"}';

    var xmlhttp = new XMLHttpRequest();
    var URL = "http://" + window.location.host + "/GDPRPersonalDataViewer/rest/getpiidata";
    xmlhttp.open("POST", URL, false);
    xmlhttp.setRequestHeader("Content-Type", "application/json");
    xmlhttp.send(inputJSON);
    var jsonData = JSON.parse(xmlhttp.responseText);
    documentscore.innerHTML = "Document Score: " + jsonData.score;
    showTree(jsonData.treeData);
}

var svg;
var i;
var treemap;
var root;
function showTree(treeData){
	console.log("treeData");
	console.log(treeData);
	//treeData = {"children":[{"children":[{"children":[{"children":[{"name":"Weightage 10.0"}],"name":"IBM"}],"name":"company"},{"children":[{"children":[{"name":"Weightage 10.0"}],"name":"10-Aug-1979"}],"name":"dob"}],"name":"Cat2"},{"children":[{"children":[{"children":[{"name":"Weightage 50.0"}],"name":"Murali"}],"name":"name"}],"name":"Cat1"}],"name":"Categories"};
	
	// Set the dimensions and margins of the diagram
	var margin = {top: 40, right: 20, bottom: 20, left: 20},
	    width = 1100 - margin.left - margin.right,
	    height = 900 - margin.top - margin.bottom;

	// append the svg object to the body of the page
	// appends a 'group' element to 'svg'
	// moves the 'group' element to the top left margin
	
	d3.select("svg").remove(); // remove the svg if already existing
	
	svg = d3.select("body").append("svg")
	    .attr("width", "100%")
	    .attr("height", "120%")
	    .append("g")
	    .attr("transform", "translate(" + 320 + "," + margin.top + ")");

	i = 0,
	duration = 750;
	   

	// declares a tree layout and assigns the size
	treemap = d3.tree().size([height, width]);

	// Assigns parent, children, height, depth
	root = d3.hierarchy(treeData, function(d) { return d.children; });
	root.x0 = height / 2;
	root.y0 = 0;

	// Collapse after the second level
	root.children.forEach(collapse);

	update(root);
}


//Collapse the node and all it's children
function collapse(d) {
  if(d.children) {
    d._children = d.children
    d._children.forEach(collapse)
    d.children = null
  }
}

function update(source) {

  // Assigns the x and y position for the nodes
  var treeData = treemap(root);

  // Compute the new tree layout.
  var nodes = treeData.descendants(),
      links = treeData.descendants().slice(1);

  // Normalize for fixed-depth.
  nodes.forEach(function(d){ d.y = d.depth * 180});

  // ****************** Nodes section ***************************

  // Update the nodes...
  var node = svg.selectAll('g.node')
      .data(nodes, function(d) {return d.id || (d.id = ++i); });

  // Enter any new modes at the parent's previous position.
  var nodeEnter = node.enter().append('g')
      .attr('class', 'node')
      .attr("transform", function(d) {
        return "translate(" + source.x0 + "," + source.y0 + ")";
    })
    .on('click', click);

  // Add Circle for the nodes
  nodeEnter.append('circle')
      .attr('class', 'node')
      .attr('r', 1e-6)
      .style("fill", function(d) {
          return d._children ? "lightsteelblue" : "#fff";
      });

  // Add labels for the nodes
  nodeEnter.append('text')
      .attr("dx", ".35em")
      .attr("y", function(d) {
          return d.children || d._children ? -13 : 13;
      })
      .attr("text-anchor", function(d) {
          return d.children || d._children ? "end" : "start";
      })
      .text(function(d) { return d.data.name; });

  // UPDATE
  var nodeUpdate = nodeEnter.merge(node);

  // Transition to the proper position for the node
  nodeUpdate.transition()
    .duration(duration)
    .attr("transform", function(d) { 
        return "translate(" + d.x + "," + d.y + ")";
     });

  // Update the node attributes and style
  nodeUpdate.select('circle.node')
    .attr('r', 10)
    .style("fill", function(d) {
        return d._children ? "lightsteelblue" : "#fff";
    })
    .attr('cursor', 'pointer');


  // Remove any exiting nodes
  var nodeExit = node.exit().transition()
      .duration(duration)
      .attr("transform", function(d) {
          return "translate(" + source.x + "," + source.y + ")";
      })
      .remove();

  // On exit reduce the node circles size to 0
  nodeExit.select('circle')
    .attr('r', 1e-6);

  // On exit reduce the opacity of text labels
  nodeExit.select('text')
    .style('fill-opacity', 1e-6);

  // ****************** links section ***************************

  // Update the links...
  var link = svg.selectAll('path.link')
      .data(links, function(d) { return d.id; });

  // Enter any new links at the parent's previous position.
  var linkEnter = link.enter().insert('path', "g")
      .attr("class", "link")
      .attr('d', function(d){
        var o = {x: source.x0, y: source.y0}
        return diagonal(o, o)
      });

  // UPDATE
  var linkUpdate = linkEnter.merge(link);

  // Transition back to the parent element position
  linkUpdate.transition()
      .duration(duration)
      .attr('d', function(d){ return diagonal(d, d.parent) });

  // Remove any exiting links
  var linkExit = link.exit().transition()
      .duration(duration)
      .attr('d', function(d) {
        var o = {x: source.x, y: source.y}
        return diagonal(o, o)
      })
      .remove();

  // Store the old positions for transition.
  nodes.forEach(function(d){
    d.x0 = d.x;
    d.y0 = d.y;
  });

  // Creates a curved (diagonal) path from parent to the child nodes
  function diagonal(s, d) {

    path = `M ${s.x} ${s.y}
            C ${(s.x + d.x) / 2} ${s.y},
              ${(s.x + d.x) / 2} ${d.y},
              ${d.x} ${d.y}`

    return path
  }

  // Toggle children on click.
  function click(d) {
    if (d.children) {
        d._children = d.children;
        d.children = null;
      } else {
        d.children = d._children;
        d._children = null;
      }
    update(d);
  }
}
