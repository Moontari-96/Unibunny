package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import commons.Pagination;
import dao.BoardDAO;
import dao.BoardLikeDAO;
import dao.BookMarkDAO;
import dao.MemberDAO;
import dao.ReplyDAO;
import dto.BoardDTO;

@WebServlet("*.board")
public class BoardController extends HttpServlet {
    private void processRequest(HttpServletRequest request, HttpServletResponse response, String cmd, String game_id, String type)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        BoardDAO dao = BoardDAO.getInstance();
        Gson gson = new GsonBuilder().setDateFormat("yyyy.MM.dd").create();
        PrintWriter pw = response.getWriter();
        try {
            String pcpage = request.getParameter("cpage");
            String game_Id = request.getParameter("gameId");
            if (pcpage == null) {
                pcpage = "1";
            }
            int cpage = Integer.parseInt(pcpage);

            List<BoardDTO> list = null;
            if ("list".equals(type)) {
                if (game_Id == null || game_Id.equals("game_id")) {
                    list = dao.selectListAll(
                            cpage * Pagination.recordCountPerPage - (Pagination.recordCountPerPage - 1),
                            cpage * Pagination.recordCountPerPage
                    );
                } else {
                    list = dao.selectListAllGame(
                            cpage * Pagination.recordCountPerPage - (Pagination.recordCountPerPage - 1),
                            cpage * Pagination.recordCountPerPage, game_Id
                    );
                }
            } else if ("like".equals(type)) {
                if (game_Id == null || game_Id.equals("game_id")) {
                    list = dao.selectListLike(
                            cpage * Pagination.recordCountPerPage - (Pagination.recordCountPerPage - 1),
                            cpage * Pagination.recordCountPerPage
                    );
                } else {
                    list = dao.selectListLikeGame(
                            cpage * Pagination.recordCountPerPage - (Pagination.recordCountPerPage - 1),
                            cpage * Pagination.recordCountPerPage, game_Id
                    );
                }
            } else if ("view".equals(type)) {
                if (game_Id == null || game_Id.equals("game_id")) {
                    list = dao.selectListView(
                            cpage * Pagination.recordCountPerPage - (Pagination.recordCountPerPage - 1),
                            cpage * Pagination.recordCountPerPage
                    );
                } else {
                    list = dao.selectListViewGame(
                            cpage * Pagination.recordCountPerPage - (Pagination.recordCountPerPage - 1),
                            cpage * Pagination.recordCountPerPage, game_Id
                    );
                }
            }

            if (list != null) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    Map<String, Object> result = new HashMap<>();
                    result.put("data", list);
                    result.put("cpage", cpage);
                    result.put("record_count_per_page", Pagination.recordCountPerPage);
                    result.put("navi_count_per_page", Pagination.naviCountPerPage);

                    if (game_Id == null || game_Id.equals("game_id")) {
                        result.put("record_total_count", dao.getRecordCount());
                    } else {
                        result.put("record_total_count", dao.getRecordCountGame(game_Id));
                    }

                    String jsonResult = gson.toJson(result);
                    PrintWriter out = response.getWriter();
                    out.print(jsonResult);
                    out.flush();
                    out.close();
                } else {
                    request.setAttribute("boardlist", list);
                    request.setAttribute("cpage", cpage);
                    request.setAttribute("record_count_per_page", Pagination.recordCountPerPage);
                    request.setAttribute("navi_count_per_page", Pagination.naviCountPerPage);
                    if (game_Id == null || game_Id.equals("game_id")) {
                        request.setAttribute("record_total_count", dao.getRecordCount());
                    } else {
                        request.setAttribute("record_total_count", dao.getRecordCountGame(game_Id));
                    }

                    request.getRequestDispatcher("/user/crud/list.jsp").forward(request, response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("/error.jsp");
        } finally {
            pw.close();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        String cmd = request.getRequestURI();
        System.out.println(cmd);
        BoardDAO dao = BoardDAO.getInstance();
        String game_Id = request.getParameter("gameId");
        Gson gson = new GsonBuilder().setDateFormat("yyyy.MM.dd").create();
        PrintWriter pw = response.getWriter();
        try {
            if (cmd.equals("/list.board")) {
                System.out.println(game_Id);
                processRequest(request, response, cmd, game_Id, "list");
            } else if (cmd.equals("/like.board")) {
                System.out.println(game_Id);
                processRequest(request, response, cmd, game_Id, "like");
            } else if (cmd.equals("/view.board")) {
                System.out.println(game_Id);
                processRequest(request, response, cmd, game_Id, "view");
            } else if (cmd.equals("/mylist.board")) {
                String pcpage = request.getParameter("cpage");
                if (pcpage == null) {
                    pcpage = "1";
                }
                int cpage = Integer.parseInt(pcpage);

                List<BoardDTO> list = dao.selectListAll(
                        cpage * Pagination.recordCountPerPage - (Pagination.recordCountPerPage - 1),
                        cpage * Pagination.recordCountPerPage);
                request.setAttribute("boardlist", list);
            } else if (cmd.equals("/user/detail.board")) {
                int board_seq = Integer.parseInt(request.getParameter("board_seq"));
                request.setAttribute("dto", dao.selectBySeq(board_seq));
                String loginID = (String) request.getSession().getAttribute("loginID");
                request.setAttribute("nickname", MemberDAO.getInstance().getNickname(loginID));
                request.getRequestDispatcher("/user/crud/detail.jsp").forward(request, response);
            } else if (cmd.equals("/tryUpdate.board")) {
                int board_seq = Integer.parseInt(request.getParameter("board_seq"));
                request.setAttribute("dto", dao.selectBySeq(board_seq));
                request.getRequestDispatcher("/user/crud/modi_board.jsp").forward(request, response);
            } else if (cmd.equals("/update.board")) {
                int board_seq = Integer.parseInt(request.getParameter("board_seq"));
                dao.update(board_seq, request.getParameter("edit_title"), request.getParameter("edit_content"));
                response.sendRedirect("/user/detail.board?board_seq=" + board_seq);
            } else if (cmd.equals("/delete.board")) {
                int board_seq = Integer.parseInt(request.getParameter("board_seq"));
                dao.deleteBySeq(board_seq);
                response.sendRedirect("/list.board");
            } else if (cmd.equals("/myboard.board")) {
                String id = (String) request.getSession().getAttribute("loginID");
                System.out.println("진입");
                String pcpage = request.getParameter("cpage");
                if (pcpage == null) {
                    pcpage = "1";
                }
                int cpage = Integer.parseInt(pcpage);
                System.out.println("회원의 게시글 조회");

                List<BoardDTO> list = dao.searchMyBoardList(cpage * Pagination.recordCountPerPage - (Pagination.recordCountPerPage - 1),
                        cpage * Pagination.recordCountPerPage, id);
                System.out.println("게시글 조회 완료");

                request.setAttribute("mylist", list);
                request.setAttribute("cpage", cpage);
                request.setAttribute("record_count_per_page", Pagination.recordCountPerPage);
                request.setAttribute("navi_count_per_page", Pagination.naviCountPerPage);
                request.setAttribute("record_total_count", dao.searchBoardCount(id));
                request.setAttribute("activeTab", "myPosts");
                request.getRequestDispatcher("/user/mypage/mypage.jsp").forward(request, response);
            } else if (cmd.equals("/myreply.board")) {
                String id = (String) request.getSession().getAttribute("loginID");
                System.out.println("진입");
                String pcpage = request.getParameter("cpage");
                if (pcpage == null) {
                    pcpage = "1";
                }
                int cpage = Integer.parseInt(pcpage);
                System.out.println("회원이 댓글 단 글 조회");

                List<BoardDTO> list = dao.searchMyCommentedBoardList(cpage * Pagination.recordCountPerPage - (Pagination.recordCountPerPage - 1),
                        cpage * Pagination.recordCountPerPage, id);
                System.out.println("게시글 조회 완료");

                request.setAttribute("myreplylist", list);
                request.setAttribute("cpage", cpage);
                request.setAttribute("record_count_per_page", Pagination.recordCountPerPage);
                request.setAttribute("navi_count_per_page", Pagination.naviCountPerPage);
                request.setAttribute("record_total_count", dao.getRecordCount());
                request.setAttribute("activeTab", "comments");
                request.getRequestDispatcher("/user/mypage/mypage.jsp").forward(request, response);
            } else if (cmd.equals("/mybookmark.board")) {
                String id = (String) request.getSession().getAttribute("loginID");
                System.out.println("진입");
                String pcpage = request.getParameter("cpage");
                if (pcpage == null) {
                    pcpage = "1";
                }
                int cpage = Integer.parseInt(pcpage);
                System.out.println("회원의 북마크 조회");

                List<BoardDTO> list = dao.searchMyBookmarkedBoardList(cpage * Pagination.recordCountPerPage - (Pagination.recordCountPerPage - 1),
                        cpage * Pagination.recordCountPerPage, id);
                System.out.println("북마크 게시글 조회 완료");

                request.setAttribute("mybookmark", list);
                request.setAttribute("cpage", cpage);
                request.setAttribute("record_count_per_page", Pagination.recordCountPerPage);
                request.setAttribute("navi_count_per_page", Pagination.naviCountPerPage);
                request.setAttribute("record_total_count", dao.getRecordCount());
                request.setAttribute("activeTab", "bookmarks");
                request.getRequestDispatcher("/user/mypage/mypage.jsp").forward(request, response);
            } else if (cmd.equals("/deletedboard.board")) {
                List<BoardDTO> list = dao.searchDeletedList();
                request.setAttribute("deletedlist", list);
                System.out.println("가져오기 완료");
                request.getRequestDispatcher("/manager/community.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("/error.jsp");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
