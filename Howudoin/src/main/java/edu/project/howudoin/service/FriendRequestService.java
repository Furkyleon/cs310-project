package edu.project.howudoin.service;

import edu.project.howudoin.model.FriendRequest;
import edu.project.howudoin.repository.FriendRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendRequestService {
    @Autowired
    private FriendRequestRepository friendRequestRepository;
    @Autowired
    private UserService userService;

    // generating id
    public int generateRequestId(){
        return (int) friendRequestRepository.count();
    }

    // sending request function
    public String sendRequest(FriendRequest request){
        String sender = request.getSender();
        String receiver = request.getReceiver();
        boolean receiverCheck = userService.userCheck(receiver);

        if (!receiverCheck) {
            return "There is no user that named " + receiver + ".";
        }
        else if (sender.equals(receiver)) {
            return "You cannot send request to yourself!";
        }
        else {
            boolean check1 = friendRequestRepository.existsBySenderAndReceiver(sender, receiver);
            boolean check2 = friendRequestRepository.existsBySenderAndReceiver(receiver, sender);

            if (check1) {
                return "You have already sent an request to this receiver.";
            } else if (check2) {
                return "This receiver already sent an request to you. You can accept the request.";
            } else {
                friendRequestRepository.save(request);
                return "Request has been sent.";
            }
        }
    }

    // accepting request function
    public String acceptRequest(String senderNickname, String receiverNickname){
        FriendRequest request;
        if (friendRequestRepository.existsBySenderAndReceiver(senderNickname, receiverNickname)) {
            request = friendRequestRepository.findBySenderAndReceiver(senderNickname, receiverNickname).get();

            friendRequestRepository.delete(request);
            request.setAccepted(true);
            friendRequestRepository.save(request);

            userService.addFriend(request);
            return "Request has been accepted.";
        }
        else {
            return "There is no such request.";
        }
    }

    // getting friends function
    public List<String> getFriends(String nickname) {
        return userService.getFriends(nickname);
    }

    public boolean checkRequest(String senderNickname, String receiverNickname) {
        FriendRequest request = friendRequestRepository.findBySenderAndReceiver(senderNickname, receiverNickname).get();
        if (request.isAccepted()) {
            return false;
        }
        else {
            return true;
        }
    }
}
