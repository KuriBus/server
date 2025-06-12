class NicknameScene extends Phaser.Scene {
  constructor() {
    super('NicknameScene');
  }

  preload() {
    this.load.image('nickname_bg', 'assets/nicknamebg.png');
    const fontLink = document.createElement('link');
    fontLink.rel = 'stylesheet';
    fontLink.href = 'https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css';
    document.head.appendChild(fontLink);
  }

  create() {
    const login = async (nickname) => {
      try {
        const response = await fetch(`${SERVER_URL}/api/users/login`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include',
          body: JSON.stringify({ nickname }),
        });

        if (!response.ok) {
          const message = await response.text();
          console.error(`로그인 실패 (${response.status}):`, message);
          throw new Error(`로그인 실패: ${message}`);
        }

        const data = await response.json();

        window.userInfo = {
          nickname: nickname,
          userId: data.userId,
          roomId: data.roomId || null,
          character: data.character || null
        };

        console.log("로그인 성공:", nickname);
        this.inputElement.remove();
        this.scene.start('CharacterSelectScene', { nickname: nickname });

      } catch (error) {
        console.error("로그인 오류:", error);
        alert("로그인 실패: 닉네임을 다시 확인해주세요.");
      }
    };

    const gameWidth = this.sys.game.config.width;
    const gameHeight = this.sys.game.config.height;
    this.add.image(gameWidth / 2, gameHeight / 2, 'nickname_bg').setDisplaySize(gameWidth, gameHeight);

    this.add.rectangle(gameWidth / 2, gameHeight / 2, 820, 279, 0xF8F2FC).setOrigin(0.5).setDepth(1);
    this.add.rectangle(gameWidth / 2, gameHeight / 2 - 120, 381, 72, 0xB593CC).setOrigin(0.5).setDepth(2);

    this.add.text(gameWidth / 2, gameHeight / 2 - 120, '당신의 이름은?', {
      fontFamily: 'Pretendard',
      fontSize: '32px',
      color: '#ffffff',
      fontStyle: 'bold',
      align: 'center'
    }).setOrigin(0.5).setDepth(3);

    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = '이름을 입력하세요';
    this.inputElement = input;

    Object.assign(input.style, {
      position: 'fixed',
      left: '50%',
      top: 'calc(50% + 20px)',
      transform: 'translate(-50%, -50%)',
      width: '489px',
      height: '60px',
      fontSize: '20px',
      padding: '16px 24px',
      border: '4px solid #B593CC',
      borderRadius: '12px',
      backgroundColor: '#ffffff',
      outline: 'none',
      zIndex: '10',
      fontFamily: 'Pretendard',
    });

    document.body.appendChild(input);

    input.addEventListener('keydown', (event) => {
      if (event.key === 'Enter') {
        const nickname = input.value.trim();
        if (!nickname) {
          alert('이름을 입력하세요.');
          return;
        }
        login(nickname);
      }
    });
  }

  shutdown() {
    if (this.inputElement) {
      this.inputElement.remove();
    }
  }
}

export default NicknameScene;