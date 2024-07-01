<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Q&A</title>
<link href="https://stackpath.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="../../../css/common.css">
<link rel="stylesheet" href="../../../css/sub.css">
<link rel="stylesheet" href="../../../css/layout.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" />
<script defer src="../../../js/common.js"></script>
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
<link href="https://cdn.jsdelivr.net/npm/summernote@0.8.18/dist/summernote.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/summernote@0.8.18/dist/summernote.min.js"></script>
</head>
<body>
    <div class="wrapper">
        <div class="header_area">
            <!-- 생략된 헤더 코드 -->
        </div>
        <div class="body_area">
            <div class="body for_pc">
                <div class="wrap">
                    <div class="con_wrap">
                        <div class="con write_con">
                            <div class="title_box">
                                <p class="title">Q&A 작성하기</p>
                            </div>
                            <form id="qnaForm" action="/write.qna" method="post"  enctype="multipart/form-data">
                                <div class="list_table">
                                    <div class="table_row table_header">
                                        <span>제목</span>
                                        <div style="padding: 5px;"></div>
                                        <input type="text" name="question_title" id="title">
                                    </div>
                                    <div style="padding: 10px;"></div>
                                    <input type="hidden" name="userId" value="${sessionScope.userId}">
                                    <div style="padding: 10px;"></div>
                                    <div id="addfile">
                                        <span>파일첨부</span>
                                        <div style="padding: 5px;"></div>
                                        <button type="button" id="file" name="files[]">+</button>
                                    </div>
                                    <div id="filebox"></div>
                                    <div style="padding: 10px;"></div>
                                    <div class="write">
                                        <textarea id="summernote" name="question_content"></textarea>
                                    </div>
                                </div>
                                <div class="btns">
                                    <button class="write_btn" type="submit">작성하기</button>
                                    <button class="list_btn" type="button" onclick="location.href='/list.faq'">돌아가기</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="footer_area">
            <!-- 생략된 푸터 코드 -->
        </div>
<script>
    $(document).ready(function() {
        // Summernote 에디터를 초기화합니다.
        $('#summernote').summernote({
            height: 500,         
            minHeight: null,     
            maxHeight: null,     
            focus: true          
        });

        
        $('#qnaForm').on('submit', function(event) {
            event.preventDefault();

            // 제목 및 내용 변수에 저장
            var questionTitle = $('#title').val().trim();
            var questionContent = $('#summernote').summernote('code').trim();

         	// 제목이 입력되지 않은 경우 얼럿창!
            if (!questionTitle) {
                alert('제목을 입력하세요.');
                return false;
            }

            // 내용이 입력되지 않은 경우 얼럿창!
            if ($('#summernote').summernote('isEmpty')) {
                alert('내용을 입력하세요.');
                return false;
            }

            var form = $(this);  // 폼 객체를 변수에 저장합니다.

            // 폼 데이터를 AJAX 요청으로 서버에 전송합니다.
            $.ajax({
                url: form.attr('action'),  // 폼의 action 속성에서 URL을 가져옵니다.
                method: 'POST',            // HTTP POST 메서드를 사용합니다.
                data: form.serialize(),    // 폼 데이터를 직렬화하여 전송합니다.
                success: function(response) {
                    alert('Q&A 작성 완료');

                    var question_seq = response.question_seq;  // 서버 응답에서 question_seq를 가져옵니다.
                    var fileInputs = $('#filebox').find('input[type="file"]');  // 파일 입력 요소를 찾습니다.

                    // 파일 입력 요소가 있는 경우
                    if (fileInputs.length > 0) {
                    	// 새로운 FormData 객체를 생성
                        var formData = new FormData();  
                        // 각 파일 입력 요소의 파일을 formData에 추가합니다.
                        fileInputs.each(function(index, fileInput) {
                            formData.append('files[]', fileInput.files[0]);
                        });
                    	 // question_seq를 formData에 추가
                        formData.append('question_seq', question_seq);  

                        // 파일 데이터를 AJAX 요청으로 서버에 전송합니다.
                        $.ajax({
                            url: '/upload.qnafile',  // 파일 업로드를 처리할 서버 URL
                            method: 'POST',          // HTTP POST 메서드를 사용합니다.
                            data: formData,          // 파일 데이터를 전송합니다.
                            processData: false,      // 데이터를 처리하지 않도록 설정합니다.
                            contentType: false,      // contentType을 false로 설정합니다.
                            success: function(fileResponse) {
                                window.location.href = '/list.faq';
                            },
                            error: function() {
                                alert('파일 업로드 중 오류 발생');
                            }
                        });
                    } else {
                        window.location.href = '/list.faq';
                    }
                },
                error: function() {
                    alert('Q&A 작성 중 오류 발생');
                }
            });
        });
    });
</script>

</body>
</html>
