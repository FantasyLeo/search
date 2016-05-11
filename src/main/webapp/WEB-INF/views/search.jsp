<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Search</title>
<link rel="shortcut icon" href="/search/favicon.ico" />
<link href="/search/bootstrap/css/bootstrap.min.css" rel="stylesheet">
<script src="/search/bootstrap/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/search/jquery/jquery-1.11.3.min.js"></script>

<style type="text/css">
.search_div1 {
	
}

.search_box {
	margin: 10px auto;
	width: 750px;
	height: 29px;
}

.search_input {
	border: 1px solid #d9d9d9;
	width: 650px;
	height: 29px;
	line-height: 29px;
	padding-left: 11px;
	padding-top: 4px;
	background: none;
	text-align: left;
	font-size: 14px
}

.search_botton {
	border: 0px;
	color: white;
	font-weight: bold;
	font-size: 16px;
	letter-spacing: 1px;
	width: 100px;
	height: 29px;
	position: relative;
	float: right;
	background: #498AF3;
}

#content {
	margin: 0 auto;
	width: 750px;
}
#cfoot {
	margin: 0 auto;
	width: 750px;
}
.item {
	width: 750px;
	/* border: 1px solid red; */
	word-wrap: break-word;
}

.item a {
	text-decoration:underline;
}
.item-abstract {
	
}

.item-foot {
	color: green;
}
</style>

<script type="text/javascript">
	$(document).ready(function() {
		search(1,3);

		$('#search_input').keydown(function(event) {
			if (event.which == 13) {
				search(1,3);
			}
		});

	});

	function search(curPage,an) {
		var q = $("#search_input").val();
		if (q != null && q != '') {
			$.ajax({
				url : "/search/search.do",
				data : {
					queryString : q,
					curPage : curPage,
					an : an
				},
				success : function(data) {
					showResult(data.results);
					showPage(data.total,data.curPage);
				},
				dataType : "json",
				method : "POST"
			});
		}

	}

	function searchTitle(curPage,an) {
		var q = $("#search_input").val();
		if (q != null && q != '') {
			$.ajax({
				url : "/search/searchTitle.do",
				data : {
					queryString : q,
					curPage : curPage,
					an : an
				},
				success : function(data) {
					showResult(data.results);
					showPage(data.total,data.curPage);
				},
				dataType : "json",
				method : "POST"
			});
		}
	}

	function searchContent(curPage,an) {
		var q = $("#search_input").val();
		if (q != null && q != '') {
			$.ajax({
				url : "/search/searchContent.do",
				data : {
					queryString : q,
					curPage : curPage,
					an : an
				},
				success : function(data) {
					showResult(data.results);
					showPage(data.total,data.curPage);
				},
				dataType : "json",
				method : "POST"
			});
		}
	}

	function qresult(data) {
		console.log("size" + data.length);
	}

	function showRow(row) {
		var item = '<div class="item">'
				+ '<a href="'+ row.url +'" target="_blank"><h5>' + row.title
				+ '</h5></a>' + '<div class="item-abstract">' + row.abstr
				+ '</div>' + '<div class="item-foot">' + row.url + '</div>'
				+ '</div>';
		$("#content").append(item);
	}

	function showResult(rows) {
		$("#content").empty();
		console.log("size" + rows.length);
		for (var i = 0; i < 10; i++) {
			showRow(rows[i]);
		}
	}

	function showPage(total,curPage) {
			var pagi = $("#pagination");
			pagi.empty();
		var pages = 10;
		if(total < 100) {
			pages = (total-1)/10 + 1;
			console.info(total +"   "  +pages);
		}

		if(pages < 1) {
			return;
		}

		if(curPage > 1) {
			pagi.append('<li><a href="javascript:search(' + (curPage -1) + ',3)" aria-label="Previous"><span aria-hidden="true">&laquo;</span></a></li>');
		}

		for(var i = 1 ; i < pages; i++ ) {
			if( i == curPage) {
				pagi.append('<li class="active" ><a href="javascript:search(' + i + ',3)">' + i +'</a></li>')
			}else {
				pagi.append('<li><a href="javascript:search(' + i + ',3)">' + i +'</a></li>');
			}
		}

		if(curPage + 1 < pages) {
			pagi.append('<li><a href="javascript:search(' + (curPage +1) + ',3)" aria-label="Previous"><span aria-hidden="true">&raquo;</span></a></li>');
		}
	}
</script>

</head>
<body>
	<div id="head">
		<div class="search_box">
			<div class="search_div1">
				<input id="search_input" type="text" class="search_input" />
				<input type="submit" class="search_botton" value="Search"
					onclick="search(1,3)" />
			</div>
		</div>
		<div class="search_box">
			<button type="button" class="btn btn-primary btn-xs" style="width:100px;" onclick="search(1,1)"> STD分词器 </button>
			<button type="button" class="btn btn-primary btn-xs" style="width:100px;" onclick="search(1,2)"> S C分词器 </button>
			<button type="button" class="btn btn-primary btn-xs" style="width:100px;" onclick="search(1,3)"> I K分词器 </button>
			<button type="button" class="btn btn-primary btn-xs" style="width:100px;" onclick="searchTitle(1,3)"> 搜索标题 </button>
			<button type="button" class="btn btn-primary btn-xs" style="width:100px;" onclick="searchContent(1,3)"> 搜索内容 </button>
		</div>
	</div>

	<div id="content" class="panel panel-default"></div>
	<div id="cfoot">
		<nav>
		<ul id="pagination" class="pagination">
		</ul>
		</nav>
	</div>
</body>
</html>