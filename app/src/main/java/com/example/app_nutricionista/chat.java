package com.example.app_nutricionista;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class chat extends Fragment {

    private FirebaseUser user;
    private DatabaseReference userRef;
    private String uid;

    private String apiKey = "AIzaSyAjKfBehT4rwNxiRcTMc10KVP4EMlTEs_I";
    private String instrucao = "Você é um renomado nutricionista brasileiro apelidado de NutriSense, que fala apenas português do Brasil. Seu papel é desenvolver planos alimentares altamente personalizados e eficazes, com foco real em transformar hábitos alimentares para melhor saúde, desempenho e bem-estar. Você deve sempre analisar criticamente a dieta atual do paciente e propor melhorias objetivas — considerando qualidade nutricional, equilíbrio de macronutrientes, variedade de alimentos, substituições mais saudáveis e ajustes práticos à rotina da pessoa. Não mantenha dietas iguais às relatadas: sempre busque otimizações claras. Durante a conversa, colete as informações em etapas, com mensagens curtas e bem separadas, para facilitar a leitura: nome, idade, peso, altura, objetivo (emagrecer, ganhar massa, etc.), restrições alimentares, rotina de treinos e hábitos alimentares diários (com horários e exemplos de refeições). Assim que todas as informações forem coletadas, elabore um plano alimentar completo e direto, com todas as refeições do dia, horários sugeridos, alimentos indicados, quantidades em gramas, sugestões de substituições e recomendações gerais. No plano alimentar, use tópicos e destaque os horários (como título em negrito), e separe as refeições com quebras de linha. Cada item da refeição deve estar em uma lista ou nova linha. Evite blocos longos de texto. No plano, não inclua introduções, saudações, mensagens motivacionais, apenas o plano alimentar, limpo e direto (pode incluir substituições e recomendações). É proibido usar frases como 'aguarde um momento', 'já volto com o plano' ou similares. Assim que terminar de coletar as informações, apenas questione se há algo a mais que o paciente deseja acrescentar, antes do envio do plano. Nunca espere por uma nova mensagem do paciente para liberar o plano. Ao receber imagens de alimentos, analise visualmente os ingredientes, descreva-os, estime suas calorias e nutrientes, e sugira melhorias saudáveis para esse prato. Caso receba perguntas fora do campo da nutrição, avise gentilmente que esse não é seu foco. Você é sempre muito gentil, amigável, profissional, claro e objetivo. Foque em **propor mudanças reais na dieta** com impacto positivo na saúde do paciente.";

    private TextView textoTemporario;
    private EditText editTarefa;
    private Button btnGo, btnAddImagem, btnGallery;
    private LinearLayout chat;
    private ScrollView scroll;

    // Variável global para armazenar o histórico de conversa:
    private List<Content> history;

    // Variável que armazenará o caminho onde a imagem será salva
    private Uri imageUrl;

    // API que permite iniciar uma atividade para obter um resultado (ex: tirar uma foto ou obter uma imagem da galeria)
    private ActivityResultLauncher<Uri> contract;
    private ActivityResultLauncher<String> galleryContract;

    private TextToSpeech textToSpeech;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        textoTemporario = view.findViewById(R.id.textoTemporario);
        editTarefa = view.findViewById(R.id.editTarefa);
        btnGo = view.findViewById(R.id.btnGo);
        btnAddImagem = view.findViewById(R.id.btnAddImagem);
        btnGallery = view.findViewById(R.id.btnGallery);
        chat = view.findViewById(R.id.chat);
        scroll = view.findViewById(R.id.scroll);

        btnGo.setEnabled(false);
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            uid = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    String nome = task.getResult().child("nome").getValue(String.class);
                    String peso = task.getResult().child("peso").getValue(String.class);
                    String altura = task.getResult().child("altura").getValue(String.class);

                    StringBuilder dadosUsuario = new StringBuilder();
                    if (nome != null) dadosUsuario.append("O nome do paciente é ").append(nome).append(". ");
                    if (peso != null) dadosUsuario.append("Seu peso é ").append(peso).append(" kg. ");
                    if (altura != null) dadosUsuario.append("Sua altura é ").append(altura).append(" cm. ");

                    if (dadosUsuario.length() > 0) {
                        instrucao = dadosUsuario + instrucao;
                    } else {
                        instrucao = "Comece perguntando o nome, peso e altura do paciente. " + instrucao;
                    }
                } else {
                    instrucao = "Comece perguntando o nome, peso e altura do paciente. " + instrucao;
                }

                btnGo.setEnabled(true);
            });
        }

        // Inicializando variável de histórico
        history = new ArrayList<>();

        // Listener para detectar quando o teclado aparece
        scroll.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int screenHeight = scroll.getRootView().getHeight();
            int keypadHeight = screenHeight - scroll.getHeight();

            // Se mais de 25% da tela for coberta, assume-se que o teclado está visível
            if (keypadHeight > screenHeight * 0.25) {
                DescerScroll();
            }
        });

        editTarefa.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                DescerScroll();
            }
        });

        editTarefa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DescerScroll();
            }
        });

        // Atribuindo um Listener ao botão de enviar mensagem
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(VerificaCampo()){
                    CallGemini();
                }
            }
        });

        // Recebendo a imagem capturada
        imageUrl = createImageUri();

        // Definindo o contrato para capturar a imagem e adicionando-a na imageView
        contract = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {

            View imagem = getLayoutInflater().inflate(R.layout.imagem, null);
            ImageView imagemComida = imagem.findViewById(R.id.imagemComida);

            imagemComida.setImageURI(null);
            imagemComida.setImageURI(imageUrl);

            chat.addView(imagem);
            CallGeminiImage();
        });

        galleryContract = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            View imagem = getLayoutInflater().inflate(R.layout.imagem, null);
                            ImageView imagemComida = imagem.findViewById(R.id.imagemComida);

                            imagemComida.setImageURI(uri);
                            chat.addView(imagem);

                            // Atualiza a variável `imageUrl` com a URI da imagem selecionada
                            imageUrl = uri;
                            CallGeminiImage();
                        }
                    }
                }
        );

        // Iniciando o processo de captura de imagem ao apertar o botão
        btnAddImagem.setOnClickListener(v -> contract.launch(imageUrl));

        // Iniciando o processo de pegar uma imagem da galeria ao apertar o botão
        btnGallery.setOnClickListener(v -> galleryContract.launch("image/*"));

        return view;
    }

    // Method que cria uma Uri para a imagem e a armazena dentro do diretório interno do app
    public Uri createImageUri() {
        File image = new File(requireContext().getFilesDir(), "camera_photos.png");
        return FileProvider.getUriForFile(requireContext(), "com.example.app_nutricionista.FileProvider", image);
    }

    // Method que converte uma imagem no formato Uri para Bitmap
    private Bitmap uriToBitmap(Uri imageUri) throws IOException {
        return MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);

    }

    // Method para fazer a requisição da API do Gemini e gerar a resposta para o usuário
    public void CallGemini(){
        EsconderTexto();
        String tarefa = AdicionaTarefa();
        TextView textResposta = AdicionaResposta();
        LimpaCampo();
        DescerScroll();

        // Configurando o modelo
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.1f;
        GenerationConfig generationConfig = configBuilder.build();

        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey, generationConfig);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Verifica se o histórico está vazio antes de adicionar a instrução do sistema
        if (history.isEmpty()) {
            Content.Builder systemInstructionBuilder = new Content.Builder();
            systemInstructionBuilder.setRole("user");
            systemInstructionBuilder.addText(instrucao);
            Content systemInstruction = systemInstructionBuilder.build();
            history.add(systemInstruction);
        }

        // Criando o chat baseado no histórico
        ChatFutures geminiChat = model.startChat(history);

        // Adiciona a mensagem do usuário ao histórico
        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.setRole("user");
        userContentBuilder.addText(tarefa);
        Content userContent = userContentBuilder.build();
        history.add(userContent);

        // Envia a mensagem para o Gemini
        ListenableFuture<GenerateContentResponse> response = geminiChat.sendMessage(userContent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();

                    // Adiciona a resposta do modelo ao histórico
                    Content.Builder modelContentBuilder = new Content.Builder();
                    modelContentBuilder.setRole("model");
                    modelContentBuilder.addText(resultText);
                    Content modelContent = modelContentBuilder.build();
                    history.add(modelContent);

                    // Atribuindo a resposta do modelo ao textResposta
                    textResposta.setText(resultText);
                    DescerScroll();

                    // Verifica se a resposta contém um plano (você pode ajustar o critério)
                    if (resultText.toLowerCase().contains("café da manhã") &&
                            resultText.toLowerCase().contains("almoço") &&
                            resultText.toLowerCase().contains("jantar") &&
                            resultText.toLowerCase().contains("recomendações")) {

                        // Salva o plano no Firebase
                        if (userRef != null) {
                            userRef.child("dieta").setValue(resultText).addOnSuccessListener(aVoid -> {
                                Log.d("Firebase", "Plano de dieta salvo com sucesso!");
                            }).addOnFailureListener(e -> {
                                Log.e("Firebase", "Erro ao salvar o plano de dieta", e);
                            });
                        }
                    }
                }
                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                    textResposta.setText("Ops, algo deu errado.\nMande uma mensagem novamente!");
                    DescerScroll();
                }
            }, requireActivity().getMainExecutor());
        }
    }

    // Method para fazer a requisição do Gemini quando receber fotos
    public void CallGeminiImage(){
        EsconderTexto();
        TextView textResposta = AdicionaResposta();
        LimpaCampo();
        DescerScroll();

        // Configurando o modelo
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.1f;
        GenerationConfig generationConfig = configBuilder.build();

        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey, generationConfig);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Verifica se o histórico está vazio antes de adicionar a instrução do sistema
        if (history.isEmpty()) {
            Content.Builder systemInstructionBuilder = new Content.Builder();
            systemInstructionBuilder.setRole("user");
            systemInstructionBuilder.addText(instrucao);
            Content systemInstruction = systemInstructionBuilder.build();
            history.add(systemInstruction);
        }

        // Criando o chat baseado no histórico
        ChatFutures geminiChat = model.startChat(history);

        // Convertendo a imagem em Uri para Bitmap
        Bitmap bitmapImagemComida = null;
        try {
            bitmapImagemComida = uriToBitmap(imageUrl);
        } catch (IOException e) {
            e.printStackTrace(); // Exibe o erro no log
            Toast.makeText(requireActivity(), "Erro ao carregar a imagem", Toast.LENGTH_SHORT).show();
        }

        // Adiciona a mensagem do usuário ao histórico
        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.setRole("user");
        userContentBuilder.addText("Me fale sobre meu prato de comida, seus ingredientes, nutrientes, calorias etc.");
        userContentBuilder.addImage(bitmapImagemComida);
        Content userContent = userContentBuilder.build();
        history.add(userContent);

        // Envia a mensagem para o Gemini
        ListenableFuture<GenerateContentResponse> response = geminiChat.sendMessage(userContent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();

                    // Adiciona a resposta do modelo ao histórico
                    Content.Builder modelContentBuilder = new Content.Builder();
                    modelContentBuilder.setRole("model");
                    modelContentBuilder.addText(resultText);
                    Content modelContent = modelContentBuilder.build();
                    history.add(modelContent);

                    // Atribuindo a resposta do modelo ao textResposta
                    textResposta.setText(resultText);
                    DescerScroll();
                }
                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                    textResposta.setText("Ops, algo deu errado.\nMande uma mensagem novamente!");
                    DescerScroll();
                }
            }, requireActivity().getMainExecutor());
        }
    }

    // Method para adicionar a mensagem do usuário ao chat
    public String AdicionaTarefa(){

        String tarefa = editTarefa.getText().toString();

        // Criando um objeto do layout da mensagem do usuário (questão)
        View questao = getLayoutInflater().inflate(R.layout.questao, null);

        // Criando um objeto do textQuestao
        TextView textQuestao = questao.findViewById(R.id.textQuestao);

        // Atribuindo a mensagem do usuário ao textQuestao
        textQuestao.setText(tarefa);

        // Adicionando o textQuestao ao chat
        chat.addView(questao);

        return tarefa;
    }

    // Method para adicionar a mensagem do modelo ao chat
    public TextView AdicionaResposta() {
        View resposta = getLayoutInflater().inflate(R.layout.resposta, null);
        TextView textResposta = resposta.findViewById(R.id.textResposta);
        Button btnAudio = resposta.findViewById(R.id.btnAudio);
        chat.addView(resposta);
        textResposta.setText("...");

        // Inicializa o TextToSpeech
        textToSpeech = new TextToSpeech(requireActivity(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Define o idioma para Português do Brasil
                int result = textToSpeech.setLanguage(new Locale("pt", "BR"));

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Idioma não suportado!");
                } else {
                    Log.d("TTS", "TextToSpeech inicializado com sucesso.");
                }
            } else {
                Log.e("TTS", "Falha ao inicializar TextToSpeech.");
            }
        });

        // Define a ação do botão para converter o texto em áudio
        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = textResposta.getText().toString();
                textToSpeech.setSpeechRate(1.2f);
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        return textResposta;
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    // Method para verificar se o input está preenchido
    public boolean VerificaCampo() {
        if (editTarefa.length() == 0) {
            Toast.makeText(requireContext(), "Campo vazio!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // Method para descer o scroll da conversa até o final (usado para o recebimento e envio de novas mensagens)
    public void DescerScroll() {
        scroll.postDelayed(() -> scroll.fullScroll(ScrollView.FOCUS_DOWN), 200);
    }

    // Method para esconder o texto explicativo após o início da conversa
    public void EsconderTexto(){
        textoTemporario.setVisibility(View.GONE);
    }

    // Method para limpar o input após o envio da mensagem
    public void LimpaCampo() {
        editTarefa.setText("");
    }
}
