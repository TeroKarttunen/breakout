sprite_0_x = $d000
sprite_0_y = $d001
sprite_1_x = $d002
sprite_1_y = $d003
sprite_x_overflow = $D010
sprite_control_register = $d015
sprite_0_color = $d027
sprite_0_pointer = $07f8
sprite_1_color = $d028
sprite_1_pointer = $07f9
sprite_sprite_collision = $D01E
sprite_background_collision = $D01F
sprite_double_width = $D01D

initialize_sprites:
  lda #%00000011 ; turn on sprite 0
  sta sprite_control_register
  lda #$00 ; color black
  sta sprite_0_color
  sta sprite_1_color
  lda #$80 ; sprite_0 location =  $2000
  sta sprite_0_pointer 
  lda #$81
  sta sprite_1_pointer
  lda #200
  sta ball_x
  sta ball_y
  sta sprite_0_x
  sta sprite_0_y
  lda #$80
  sta bat_x
  sta sprite_1_x
  lda #$F0
  sta bat_y
  sta sprite_1_y
  lda #$00
  sta ball_x_overflow
  sta bat_x_overflow
  lda #%00000010
  sta sprite_double_width
  rts

inc_ball_x:
  lda ball_x_overflow
  bne inc_ball_x_overflow
  lda ball_x
  cmp #$ff
  bne +
  lda #$01
  sta ball_x_overflow
+ inc ball_x
  clc
  rts
inc_ball_x_overflow:
   lda ball_x
   cmp #$52
   bcc +
   sec
   rts
+  inc ball_x
   clc
   rts
  
dec_ball_x:
  lda ball_x_overflow
  bne dec_ball_x_overflow
  lda ball_x 
  cmp #$19
  bcc +
  dec ball_x 
  clc
  rts
+ sec
  rts
dec_ball_x_overflow:
  lda ball_x
  cmp #$00
  bne +
  lda #$00
  sta ball_x_overflow
+ dec ball_x
  clc
  rts
     
inc_ball_y:
  lda ball_y
  cmp #$FB
  bcc +
  sec
  rts
+ clc
  inc ball_y
  rts

dec_ball_y:
  lda ball_y 
  cmp #$3A
  bcc +
  clc
  dec ball_y
  rts
+ sec
  rts

inc_bat_x:
  lda bat_x_overflow
  bne inc_bat_x_overflow
  lda bat_x
  cmp #$ff
  bne +
  lda #$01
  sta bat_x_overflow
+ inc bat_x
  clc
  rts
inc_bat_x_overflow:
   lda bat_x
   cmp #$28
   bcc +
   sec
   rts
+  inc bat_x
   clc
   rts
  
dec_bat_x:
  lda bat_x_overflow
  bne dec_bat_x_overflow
  lda bat_x 
  cmp #$19
  bcc +
  dec bat_x 
  clc
  rts
+ sec
  rts
dec_bat_x_overflow:
  lda bat_x
  cmp #$00
  bne +
  lda #$00
  sta bat_x_overflow
+ dec bat_x
  clc
  rts
     
inc_bat_y:
  lda bat_y
  cmp #$F4
  bcc +
  sec
  rts
+ clc
  inc bat_y
  rts

dec_bat_y:
  lda bat_y 
  cmp #$3A
  bcc +
  clc
  dec bat_y
  rts
+ sec
  rts

  
set_ball_and_bat:
  lda ball_x
  sta sprite_0_x
  lda ball_y
  sta sprite_0_y
  lda bat_x
  sta sprite_1_x
  lda bat_y
  sta sprite_1_y
  lda bat_x_overflow
  beq +
  lda ball_x_overflow 
  ora #%00000010
  sta sprite_x_overflow
  rts
+ lda ball_x_overflow
  sta sprite_x_overflow
  rts
  
* = $2000
;sprite_0: ball
!byte $78,$00,$00,$fc,$00,$00,$fc,$00
!byte $00,$fc,$00,$00,$fc,$00,$00,$78
!byte $00,$00,$00,$00,$00,$00,$00,$00
!byte $00,$00,$00,$00,$00,$00,$00,$00
!byte $00,$00,$00,$00,$00,$00,$00,$00
!byte $00,$00,$00,$00,$00,$00,$00,$00
!byte $00,$00,$00,$00,$00,$00,$00,$00
!byte $00,$00,$00,$00,$00,$00,$00,$01

;sprite_1 ; bat
!byte $7f,$ff,$fe,$ff,$ff,$ff,$b8,$f0 
!byte $e3,$b8,$f0,$e3,$ff,$ff,$ff,$7f,$ff,$fe,$00 
!byte $00,$00,$00,$00,$00
!byte $00,$00,$00,$00,$00,$00,$00,$00
!byte $00,$00,$00,$00,$00,$00,$00,$00
!byte $00,$00,$00,$00,$00,$00,$00,$00
!byte $00,$00,$00,$00,$00,$00,$00,$00
!byte $00,$00,$00,$00,$00,$00,$00,$01
