import React from 'react';
// 컴포넌트
import Modal from '@/components/Modal';

export default function TOSModal({ isOpenModal, onCloseModal, type }) {
  const tosContent = () => {
    switch (type) {
      case 'service':
        return {
          title: '서비스 이용약관',
          content: (
            <div className='text-sm text-gray-600 max-h-[500px] overflow-y-auto'>
              <p>제1조 (목적)</p>
              <p className='pb-[12px]'>
                본 약관은 회사가 제공하는 서비스 이용과 관련하여 회원과 회사 간의 권리, 의무 및 책임
                사항을 규정합니다.
              </p>

              <p>제2조 (회원가입)</p>
              <p className='pb-[12px]'>
                회원은 이름, 이메일, 비밀번호, 성별, 출생연도(나이), 키, 현재 체중, 목표 체중(상황에 따라
                필수), 활동량(상황에 따라 필수)을 정확히 입력해야 하며, 허위 정보를 기재할 경우
                서비스 이용이 제한될 수 있습니다.
              </p>

              <p>제3조 (서비스 제공)</p>
              <p className='pb-[12px]'>
                회사는 회원의 개인정보를 기반으로 맞춤형 식단 관리, 활동 분석 및 건강 목표 관리
                서비스를 제공합니다.
              </p>

              <p>제4조 (회원의 의무)</p>
              <p className='pb-[12px]'>
                회원은 비밀번호를 안전하게 관리해야 하며, 타인의 개인정보를 도용하거나 허위 정보를
                입력해서는 안 됩니다.
              </p>

              <p>제5조 (개인정보 보호)</p>
              <p className='pb-[12px]'>
                회사는 관련 법령을 준수하며, 회원의 개인정보를 안전하게 보호합니다. 자세한 내용은
                개인정보 처리방침에 따릅니다.
              </p>

              <p>제6조 (서비스 제한 및 해지)</p>
              <p className='pb-[12px]'>
                회원이 약관을 위반하는 경우 회사는 서비스 이용을 제한하거나 회원 자격을 해지할 수
                있습니다.
              </p>
            </div>
          ),
        };
      case 'privacy':
        return {
          title: '개인정보 처리 방침',
          content: (
            <div className='text-sm text-gray-600 max-h-[500px] overflow-y-auto'>
              <p>
                오늘의 냠냠은 회원가입 및 서비스 제공을 위하여 아래와 같이 개인정보를
                수집·이용합니다.
              </p>

              <p className='pt-[12px]'>1. 수집 항목</p>
              <p className='pl-[12px]'>- 이름, 이메일(ID), 비밀번호</p>
              <p className='pl-[12px]'>- 성별, 출생연도(나이), 키, 현재 체중, 목표 체중, 활동량</p>

              <p className='pt-[12px]'>2. 수집 목적</p>
              <p className='pl-[12px]'>- 회원가입 및 본인 확인</p>
              <p className='pl-[12px]'>- 서비스 제공 및 회원 관리</p>

              <p className='pt-[12px]'>3. 보유 및 이용 기간</p>
              <p className='pl-[12px]'>- 회원 탈퇴 시 즉시 삭제</p>
              <p className='pl-[12px]'>
                - 단, 관련 법령에 따라 일정 기간 보관이 필요한 경우 해당 법령에 따름
              </p>
              <p className='pl-[24px]'>- 계약 또는 청약철회 등에 관한 기록: 5년 (전자상거래법)</p>
              <p className='pl-[24px]'>
                - 부정이용 및 비정상 서비스 이용기록: 1년 (개인정보보호법)
              </p>

              <p className='pt-[12px]'>4. 동의 거부권 안내</p>
              <p className='pl-[12px]'>- 회원은 개인정보 수집·이용에 동의하지 않을 수 있습니다.</p>
              <p className='pl-[12px]'>
                - 단, 필수 항목에 동의하지 않을 경우 회원가입이 제한됩니다.
              </p>
            </div>
          ),
        };
      case 'sensitive':
        return {
          title: '민감정보 처리 방침',
          content: (
            <div className='text-sm text-gray-600 max-h-[500px] overflow-y-auto'>
              <p>1. 수집 항목</p>
              <p className='pl-[12px]'>- 키, 현재 체중, 목표 체중, 목표 유형</p>
              <p className='pl-[12px] pb-[12px]'>- 식단 및 수분 기록, 칼로리·영양성분</p>

              <p>2. 수집 목적</p>
              <p className='pl-[12px]'>- 맞춤형 건강/식단 관리 서비스 제공</p>
              <p className='pl-[12px] pb-[12px]'>- 체중·영양·활동량 분석 및 AI 피드백 제공</p>

              <p>3. 보관 기간</p>
              <p className='pl-[12px]'>- 회원 탈퇴 시 즉시 삭제</p>
              <p className='pl-[12px] pb-[12px]'>
                - 단, 관련 법령에 따라 일정 기간 보관 필요 시 해당 법령에 따름
              </p>

              <p>4. 동의 거부권 안내</p>
              <p className='pl-[12px]'>- 회원은 개인정보 수집·이용에 동의하지 않을 수 있습니다.</p>
              <p className='pl-[12px] pb-[12px]'>
                - 단, 필수 항목에 동의하지 않을 경우 회원가입이 제한됩니다.
              </p>
            </div>
          ),
        };
      default:
        return { title: '', body: null };
    }
  };

  const { title, content } = tosContent();

  return (
    <Modal
      isOpenModal={isOpenModal}
      onCloseModal={onCloseModal}
      title={title}
      showClose={true}
      btnLabel='확인'
      onBtnClick={onCloseModal}
    >
      {content}
    </Modal>
  );
}
