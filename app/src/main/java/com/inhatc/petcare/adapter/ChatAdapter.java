package com.inhatc.petcare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.petcare.R;
import com.inhatc.petcare.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> chatMessages;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        
        if (chatMessage.isUser()) {
            holder.userMessageLayout.setVisibility(View.VISIBLE);
            holder.botMessageLayout.setVisibility(View.GONE);
            holder.userMessageText.setText(chatMessage.getMessage());
        } else {
            holder.userMessageLayout.setVisibility(View.GONE);
            holder.botMessageLayout.setVisibility(View.VISIBLE);
            holder.botMessageText.setText(chatMessage.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        View userMessageLayout;
        View botMessageLayout;
        TextView userMessageText;
        TextView botMessageText;

        ChatViewHolder(View itemView) {
            super(itemView);
            userMessageLayout = itemView.findViewById(R.id.user_message_layout);
            botMessageLayout = itemView.findViewById(R.id.bot_message_layout);
            userMessageText = itemView.findViewById(R.id.user_message_text);
            botMessageText = itemView.findViewById(R.id.bot_message_text);
        }
    }
}


