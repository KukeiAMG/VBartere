import { Component, AfterViewInit } from '@angular/core';

@Component({
  selector: 'app-animated-background',
  templateUrl: './animated-background.component.html',
  styleUrls: ['./animated-background.component.css']
})
export class AnimatedBackgroundComponent implements AfterViewInit {

  ngAfterViewInit(): void {
    const canvas = document.getElementById('animatedBackground') as HTMLCanvasElement;

    if (!canvas) {
      console.error('Элемент canvas не найден');
      return;
    }

    const ctx = canvas.getContext('2d');

    if (!ctx) {
      console.error('Не удалось получить контекст canvas');
      return;
    }

    // Устанавливаем размеры canvas равными размерам окна
    const resizeCanvas = () => {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    };

    resizeCanvas(); // Инициализация размеров

    const particlesArray: Particle[] = [];

    class Particle {
      x: number;
      y: number;
      size: number;
      speedX: number;
      speedY: number;

      constructor() {
        this.x = Math.random() * canvas.width;
        this.y = Math.random() * canvas.height;
        this.size = Math.random() * 5 + 1;
        this.speedX = Math.random() * 3 - 1.5;
        this.speedY = Math.random() * 3 - 1.5;
      }

      update(): void {
        this.x += this.speedX;
        this.y += this.speedY;

        if (this.x < 0 || this.x > canvas.width) {
          this.speedX = -this.speedX;
        }
        if (this.y < 0 || this.y > canvas.height) {
          this.speedY = -this.speedY;
        }
      }

      draw(): void {
        ctx!.fillStyle = `white`;
        ctx!.beginPath();
        ctx!.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        ctx!.closePath();
        ctx!.fill();
      }
    }

    function init(): void {
      particlesArray.length = 0; // Очищаем массив частиц
      for (let i = 0; i < 100; i++) {
        particlesArray.push(new Particle());
      }
    }

    function animate(): void {
      ctx!.clearRect(0, 0, canvas.width, canvas.height);
      for (let i = 0; i < particlesArray.length; i++) {
        particlesArray[i].update();
        particlesArray[i].draw();
      }
      requestAnimationFrame(animate);
    }

    init();
    animate();

    // Обработка изменения размера окна
    window.addEventListener('resize', () => {
      resizeCanvas();
      init();
    });
  }
}
